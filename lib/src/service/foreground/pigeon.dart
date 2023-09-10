import 'dart:convert';
import 'dart:ui';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:logging/logging.dart';
import 'package:moxxy_native/pigeon/service.g.dart';
import 'package:moxxy_native/src/service/config.dart';
import 'package:moxxy_native/src/service/datasender/pigeon.dart';
import 'package:moxxy_native/src/service/datasender/types.dart';
import 'package:moxxy_native/src/service/entrypoints/pigeon.dart';
import 'package:moxxy_native/src/service/foreground/base.dart';

class PigeonForegroundService extends ForegroundService {
  PigeonForegroundService() {
    _dataSender = PigeonForegroundServiceDataSender(_api);
  }

  /// Pigeon channel to the native side.
  final MoxxyServiceApi _api = MoxxyServiceApi();

  /// A method channel for background service -> UI isolate communication.
  final MethodChannel _channel =
      const MethodChannel('org.moxxy.moxxy_native/foreground');

  /// The data sender backing this class.
  late final PigeonForegroundServiceDataSender _dataSender;

  /// A logger.
  final Logger _log = Logger('PigeonForegroundService');

  @override
  Future<void> attach(
    HandleEventCallback handleData,
  ) async {
    _channel.setMethodCallHandler((call) async {
      await handleData(
        jsonDecode(call.arguments! as String) as Map<String, dynamic>,
      );
    });
  }

  @override
  Future<void> start(
    ServiceConfig config,
    HandleEventCallback uiHandleData,
  ) async {
    await _api.configure(
      PluginUtilities.getCallbackHandle(
        pigeonEntrypoint,
      )!
          .toRawHandle(),
      config.toString(),
    );

    // Prepare the method channel
    await attach(uiHandleData);

    // Start the service
    await _api.start();
    _log.finest('Background service started...');
  }

  @override
  Future<bool> isRunning() async {
    WidgetsFlutterBinding.ensureInitialized();
    return _api.isRunning();
  }

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
