import 'dart:async';
import 'dart:convert';
import 'dart:isolate';
import 'dart:ui';
import 'package:logging/logging.dart';
import 'package:moxxy_native/src/service/config.dart';
import 'package:moxxy_native/src/service/datasender/isolate.dart';
import 'package:moxxy_native/src/service/datasender/types.dart';
import 'package:moxxy_native/src/service/entrypoints/isolate.dart';
import 'package:moxxy_native/src/service/foreground/base.dart';

class IsolateForegroundService extends ForegroundService {
  /// The port on which we receive data from the isolate.
  final ReceivePort _receivePort = ReceivePort();

  /// The port on which we send data to the isolate.
  late final SendPort _sendPort;

  /// A completer that indicates when _sendPort has been set.
  /// For more notes, see the comment in [start].
  Completer<void>? _sendPortCompleter = Completer<void>();

  /// The data sender backing this class.
  late final IsolateForegroundServiceDataSender _dataSender;

  /// A logger.
  final Logger _log = Logger('IsolateForegroundService');

  @override
  Future<void> attach(
    HandleEventCallback handleData,
  ) async {
    _receivePort.asBroadcastStream().listen((data) async {
      if (data is SendPort) {
        // Set the send port.
        _sendPort = data;

        // Resolve the waiting future.
        assert(
          _sendPortCompleter != null,
          '_sendPort should only be received once!',
        );
        _sendPortCompleter?.complete();
        return;
      }

      await handleData(
        jsonDecode(data! as String) as Map<String, dynamic>,
      );
    });
  }

  @override
  Future<void> start(
    ServiceConfig config,
    HandleEventCallback uiHandleData,
  ) async {
    // Listen for events
    await attach(uiHandleData);

    await Isolate.spawn(
      isolateEntrypoint,
      [
        _receivePort.sendPort,
        config.toString(),
        RootIsolateToken.instance!,
      ],
    );

    // Wait for [_sendPort] to get set.
    // The issue is that [_receivePort] provides a stream that only one listener can listen to.
    // This means that we cannot do `await _receivePort.first`. To work around this, we just cram
    // an approximation of `_receivePort.first` into the actual listener.
    await _sendPortCompleter!.future;
    _sendPortCompleter = null;

    // Create the data sender
    _dataSender = IsolateForegroundServiceDataSender(_sendPort);
    _log.finest('Background service started...');
  }

  @override
  Future<bool> isRunning() async => false;

  @override
  ForegroundServiceDataSender getDataSender() => _dataSender;

  @override
  Future<BackgroundEvent?> send(
    BackgroundCommand command, {
    bool awaitable = true,
  }) {
    return _dataSender.sendData(
      command,
      awaitable: awaitable,
    );
  }
}
