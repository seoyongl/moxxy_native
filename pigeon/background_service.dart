import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/pigeon/background_service.g.dart',
    kotlinOut:
        'android/src/main/kotlin/org/moxxy/moxxy_native/service/background/BackgroundServiceApi.kt',
    kotlinOptions: KotlinOptions(
      package: 'org.moxxy.moxxy_native.service.background',
    ),
  ),
)

@HostApi()
abstract class MoxxyBackgroundServiceApi {
  int getHandler();

  String getExtraData();

  void setNotificationBody(String body);

  void sendData(String data);

  void stop();
}
