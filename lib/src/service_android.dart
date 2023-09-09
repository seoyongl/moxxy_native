import 'package:flutter/cupertino.dart';
import 'package:get_it/get_it.dart';
import 'package:moxxy_native/pigeon/background_service.g.dart';
import 'package:moxxy_native/src/service.dart';

@pragma('vm:entry-point')
Future<void> androidEntrypoint() async {
  // ignore: avoid_print
  print('androidEntrypoint: Called on new FlutterEngine');

  // Pull and deserialize the extra data passed on.
  WidgetsFlutterBinding.ensureInitialized();
  final config = ServiceConfig.fromString(
    await MoxxyBackgroundServiceApi().getExtraData(),
  );

  // Setup the background service
  final srv = BackgroundService();
  GetIt.I.registerSingleton(srv);
  srv.init(config);
}
