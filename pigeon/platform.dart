import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/pigeon/platform.g.dart',
    kotlinOut:
        'android/src/main/kotlin/org/moxxy/moxxy_native/platform/PlatformApi.kt',
    kotlinOptions: KotlinOptions(
      package: 'org.moxxy.moxxy_native.platform',
    ),
  ),
)
@HostApi()
abstract class MoxxyPlatformApi {
  String getPersistentDataPath();

  String getCacheDataPath();

  void openBatteryOptimisationSettings();

  bool isIgnoringBatteryOptimizations();
}
