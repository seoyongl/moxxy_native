import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/pigeon/media.g.dart',
    kotlinOut: 'android/src/main/kotlin/org/moxxy/moxxy_native/media/MediaApi.kt',
    kotlinOptions: KotlinOptions(
      package: 'org.moxxy.moxxy_native.media',
    ),
  ),
)

@HostApi()
abstract class MoxxyMediaApi {
  bool generateVideoThumbnail(String src, String dest, int maxWidth);
}
