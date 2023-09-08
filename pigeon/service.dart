import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/pigeon/service.g.dart',
    kotlinOut:
        'android/src/main/kotlin/org/moxxy/moxxy_native/service/ServiceApi.kt',
    kotlinOptions: KotlinOptions(
      package: 'org.moxxy.moxxy_native.service',
    ),
  ),
)

@HostApi()
abstract class MoxxyServiceApi {
  void configure(int handle, String extraData);

  bool isRunning();

  void start();

  void sendData(String data);
}
