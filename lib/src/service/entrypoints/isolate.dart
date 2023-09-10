import 'dart:isolate';
import 'package:flutter/services.dart';
import 'package:get_it/get_it.dart';
import 'package:moxxy_native/src/service/background/base.dart';
import 'package:moxxy_native/src/service/background/isolate.dart';
import 'package:moxxy_native/src/service/config.dart';

@pragma('vm:entry-point')
Future<void> isolateEntrypoint(dynamic parameters) async {
  parameters as List<dynamic>;

  final sendPort = parameters[0] as SendPort;
  final config = ServiceConfig.fromString(parameters[1] as String);

  // This allows us to use the root isolate's method channels.
  // See https://medium.com/flutter/introducing-background-isolate-channels-7a299609cad8
  BackgroundIsolateBinaryMessenger.ensureInitialized(
    parameters[2] as RootIsolateToken,
  );

  // Set up the background service
  final srv = IsolateBackgroundService(sendPort);
  GetIt.I.registerSingleton<BackgroundService>(srv);

  // Reply back with the new send port
  sendPort.send(srv.receivePort.sendPort);

  // Run the entrypoint
  await srv.init(config);
}
