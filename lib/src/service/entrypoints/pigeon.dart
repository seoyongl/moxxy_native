import 'package:flutter/cupertino.dart';
import 'package:get_it/get_it.dart';
import 'package:moxxy_native/pigeon/background_service.g.dart';
import 'package:moxxy_native/src/service/background/base.dart';
import 'package:moxxy_native/src/service/background/pigeon.dart';
import 'package:moxxy_native/src/service/config.dart';

/// An entrypoint that should be used when the service runs
/// in a new Flutter Engine.
@pragma('vm:entry-point')
Future<void> pigeonEntrypoint(dynamic _) async {
  // ignore: avoid_print
  print('androidEntrypoint: Called on new FlutterEngine');

  // Pull and deserialize the extra data passed on.
  WidgetsFlutterBinding.ensureInitialized();
  final config = ServiceConfig.fromString(
    await MoxxyBackgroundServiceApi().getExtraData(),
  );

  // Setup the background service
  final srv = PigeonBackgroundService();
  GetIt.I.registerSingleton<BackgroundService>(srv);
  await srv.init(config);
}
