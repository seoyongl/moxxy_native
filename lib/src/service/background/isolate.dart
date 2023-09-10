import 'dart:convert';
import 'dart:isolate';
import 'dart:ui';
import 'package:logging/logging.dart';
import 'package:moxlib/moxlib.dart';
import 'package:moxxy_native/src/service/background/base.dart';
import 'package:moxxy_native/src/service/config.dart';
import 'package:moxxy_native/src/service/datasender/types.dart';
import 'package:uuid/uuid.dart';

class IsolateBackgroundService extends BackgroundService {
  IsolateBackgroundService(this._sendPort);
  final SendPort _sendPort;
  final ReceivePort receivePort = ReceivePort();

  /// A logger.
  final Logger _log = Logger('IsolateBackgroundService');

  @override
  Future<void> send(BackgroundEvent event, {String? id}) async {
    final data = DataWrapper(
      id ?? const Uuid().v4(),
      event,
    );

    _sendPort.send(jsonEncode(data.toJson()));
  }

  @override
  Future<void> init(
    ServiceConfig config,
  ) async {
    // Ensure that the Dart executor is ready to use plugins
    // NOTE: We're not allowed to use this here. Maybe reusing the RootIsolateToken
    //       (See IsolateForegroundService) helps?
    // WidgetsFlutterBinding.ensureInitialized();
    DartPluginRegistrant.ensureInitialized();

    // Register the channel for Foreground -> Service communication
    receivePort.listen((data) async {
      // TODO(Unknown): Maybe do something smarter like pigeon and use Lists instead of Maps
      await config
          .handleData(jsonDecode(data! as String) as Map<String, dynamic>);
    });

    // Start execution
    _log.finest('Setup complete. Calling main entrypoint...');
    await config.entrypoint(config.initialLocale);
  }

  @override
  void setNotificationBody(String body) {}
}
