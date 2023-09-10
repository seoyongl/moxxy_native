import 'dart:convert';
import 'dart:ui';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:logging/logging.dart';
import 'package:moxlib/moxlib.dart';
import 'package:moxxy_native/pigeon/background_service.g.dart';
import 'package:moxxy_native/src/service/background/base.dart';
import 'package:moxxy_native/src/service/config.dart';
import 'package:moxxy_native/src/service/datasender/types.dart';
import 'package:uuid/uuid.dart';

class PigeonBackgroundService extends BackgroundService {
  final MoxxyBackgroundServiceApi _api = MoxxyBackgroundServiceApi();

  /// A method channel for Foreground -> Service communication
  // TODO(Unknown): Move this into a constant for reuse
  final MethodChannel _channel =
      const MethodChannel('org.moxxy.moxxy_native/background');

  /// A logger.
  final Logger _log = Logger('PigeonBackgroundService');

  @override
  Future<void> send(BackgroundEvent event, {String? id}) async {
    final data = DataWrapper(
      id ?? const Uuid().v4(),
      event,
    );

    await _api.sendData(jsonEncode(data.toJson()));
  }

  @override
  Future<void> init(
    ServiceConfig config,
  ) async {
    // Ensure that the Dart executor is ready to use plugins
    WidgetsFlutterBinding.ensureInitialized();
    DartPluginRegistrant.ensureInitialized();

    // Register the channel for Foreground -> Service communication
    _channel.setMethodCallHandler((call) async {
      // TODO(Unknown): Maybe do something smarter like pigeon and use Lists instead of Maps
      final args = call.arguments! as String;
      await config.handleData(jsonDecode(args) as Map<String, dynamic>);
    });

    // Start execution
    _log.finest('Setup complete. Calling main entrypoint...');
    await config.entrypoint(config.initialLocale);
  }

  @override
  void setNotificationBody(String body) {
    _api.setNotificationBody(body);
  }
}
