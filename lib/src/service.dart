import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:ui';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:logging/logging.dart';
import 'package:moxlib/moxlib.dart';
import 'package:moxxy_native/moxxy_native.dart';
import 'package:moxxy_native/src/service_android.dart';
import 'package:uuid/uuid.dart';

typedef EntrypointCallback = Future<void> Function(String initialLocale);
typedef HandleEventCallback = Future<void> Function(Map<String, dynamic>? data);

abstract class BackgroundCommand implements JsonImplementation {}

abstract class BackgroundEvent implements JsonImplementation {}

class ServiceConfig {
  const ServiceConfig(
    this.entrypoint,
    this.handleData,
    this.initialLocale,
  );

  factory ServiceConfig.fromString(String rawData) {
    final data = jsonDecode(rawData) as Map<String, dynamic>;
    return ServiceConfig(
      PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(
          data['entrypoint']! as int,
        ),
      )! as EntrypointCallback,
      PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(
          data['handleData']! as int,
        ),
      )! as HandleEventCallback,
      data['initialLocale']! as String,
    );
  }

  final String initialLocale;
  final EntrypointCallback entrypoint;
  final HandleEventCallback handleData;

  @override
  String toString() {
    return jsonEncode({
      'entrypoint': PluginUtilities.getCallbackHandle(entrypoint)!.toRawHandle(),
      'handleData': PluginUtilities.getCallbackHandle(handleData)!.toRawHandle(),
      'initialLocale': initialLocale,
    });
  }
}

/// Wrapper API that is only available to the background service.
class BackgroundService {
  final MoxxyBackgroundServiceApi _api = MoxxyBackgroundServiceApi();

  /// A method channel for Foreground -> Service communication
  // TODO(Unknown): Move this into a constant for reuse
  final MethodChannel _channel = MethodChannel('org.moxxy.moxxy_native/background');

  /// A logger.
  final Logger _log = Logger('BackgroundService');

  Future<void> send(BackgroundEvent event, {String? id}) async {
    final data = DataWrapper(
      id ?? const Uuid().v4(),
      event,
    );

    await _api.sendData(jsonEncode(data.toJson()));
  }

  void init(
    ServiceConfig config,
  ) {
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
    config.entrypoint(config.initialLocale);
  }

  void setNotificationBody(String body) {
    _api.setNotificationBody(body);
  }
}

class ForegroundServiceDataSender extends AwaitableDataSender<BackgroundCommand, BackgroundEvent> {
  ForegroundServiceDataSender(this._api); 
  final MoxxyServiceApi _api;

  @override
  Future<void> sendDataImpl(DataWrapper<JsonImplementation> data) {
    return _api.sendData(jsonEncode(data.toJson()));
  }
}

/// Wrapper API that is only available to the UI isolate.
// TODO(Unknown): Dumb naming. Name it something better
class ForegroundService {
  ForegroundService() {
    dataSender = ForegroundServiceDataSender(_api);
  }

  final MoxxyServiceApi _api = MoxxyServiceApi();

  /// A method channel for background service -> UI isolate communication.
  final MethodChannel _channel = MethodChannel('org.moxxy.moxxy_native/foreground');

  late final ForegroundServiceDataSender dataSender;

  /// A logger.
  final Logger _log = Logger('ForegroundService');

  Future<void> attach(
    HandleEventCallback handleData,    
  ) async {
    _channel.setMethodCallHandler((call) async {
      await handleData(
        jsonDecode(call.arguments! as String) as Map<String, dynamic>,
      );
    });
  }

  Future<void> start(
    ServiceConfig config, HandleEventCallback uiHandleData,
  ) async {
    int platformEntrypointHandle;
    if (Platform.isAndroid) {
      platformEntrypointHandle = PluginUtilities.getCallbackHandle(
        androidEntrypoint,
      )!.toRawHandle();
    } else {
      // TODO: Custom exception
      throw Exception('Unsupported platform');
    }

    // Configure the service on the native side
    await _api.configure(platformEntrypointHandle, config.toString());

    // Prepare the method channel
    await attach(uiHandleData);

    // Start the service
    await _api.start();
    _log.finest('Background service started...');
  }

  /// Returns true if the background service is already running. False, if not.
  Future<bool> isRunning() async {
    WidgetsFlutterBinding.ensureInitialized();
    return _api.isRunning();
  }
}
