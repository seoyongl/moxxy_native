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
class ShareItem {
  const ShareItem(this.path, this.mime, this.text);
  final String? path;
  final String mime;
  final String? text;
}

@HostApi()
abstract class MoxxyPlatformApi {
  String getPersistentDataPath();

  String getCacheDataPath();

  void openBatteryOptimisationSettings();

  bool isIgnoringBatteryOptimizations();

  void shareItems(List<ShareItem> items, String genericMimeType);
}
