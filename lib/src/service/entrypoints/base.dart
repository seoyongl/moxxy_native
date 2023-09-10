import 'dart:io';
import 'package:moxxy_native/src/service/config.dart';
import 'package:moxxy_native/src/service/entrypoints/isolate.dart';
import 'package:moxxy_native/src/service/entrypoints/pigeon.dart';
import 'package:moxxy_native/src/service/exceptions.dart';

typedef PlatformEntrypointCallback = Future<void> Function(dynamic);

ServiceConfig getServiceConfig(
  HandleEventCallback srvHandleData,
  HandleEventCallback uiHandleData,
  String initialLocale,
) {
  PlatformEntrypointCallback entrypoint;
  if (Platform.isAndroid) {
    entrypoint = pigeonEntrypoint;
  } else if (Platform.isLinux || Platform.isWindows || Platform.isMacOS) {
    entrypoint = isolateEntrypoint;
  } else {
    throw UnsupportedPlatformException();
  }

  return ServiceConfig(
    entrypoint,
    srvHandleData,
    initialLocale,
  );
}
