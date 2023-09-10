import 'dart:io';
import 'package:moxlib/moxlib.dart';
import 'package:moxxy_native/pigeon/service.g.dart';
import 'package:moxxy_native/src/service/datasender/pigeon.dart';
import 'package:moxxy_native/src/service/exceptions.dart';

typedef ForegroundServiceDataSender
    = AwaitableDataSender<BackgroundCommand, BackgroundEvent>;

abstract class BackgroundCommand implements JsonImplementation {}

abstract class BackgroundEvent implements JsonImplementation {}

ForegroundServiceDataSender getForegroundDataSender(MoxxyServiceApi api) {
  if (Platform.isAndroid) {
    return PigeonForegroundServiceDataSender(api);
  } else {
    throw UnsupportedPlatformException();
  }
}
