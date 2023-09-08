import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/pigeon/picker.g.dart',
    kotlinOut: 'android/src/main/kotlin/org/moxxy/moxxy_native/picker/PickerApi.kt',
    kotlinOptions: KotlinOptions(
      package: 'org.moxxy.moxxy_native.picker',
    ),
  ),
)

enum FilePickerType {
  /// Pick only image(s)
  image,

  /// Pick only video(s)
  video,

  /// Pick image(s) and video(s)
  imageAndVideo,

  /// Pick any kind of file(s)
  generic,
}

@HostApi()
abstract class MoxxyPickerApi {
  /// Open either the photo picker or the generic file picker to get a list of paths that were
  /// selected and are accessable. If the list is empty, then the user dismissed the picker without
  /// selecting anything.
  ///
  /// [type] specifies what kind of file(s) should be picked.
  ///
  /// [multiple] controls whether multiple files can be picked (true) or just a single file
  /// is enough (false).
  @async
  List<String> pickFiles(FilePickerType type, bool multiple);

  /// Like [pickFiles] but sets multiple to false and returns the raw binary data from the file.
  @async
  Uint8List? pickFileWithData(FilePickerType type);
}
