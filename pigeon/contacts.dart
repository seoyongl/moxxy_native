import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/pigeon/contacts.g.dart',
    kotlinOut:
        'android/src/main/kotlin/org/moxxy/moxxy_native/contacts/ContactsApi.kt',
    kotlinOptions: KotlinOptions(
      package: 'org.moxxy.moxxy_native.contacts',
    ),
  ),
)

/// The type of icon to use when no avatar path is provided.
enum FallbackIconType {
  none,
  person,
  notes;
}

@HostApi()
abstract class MoxxyContactsApi {
  void recordSentMessage(
    String name,
    String jid,
    String? avatarPath,
    FallbackIconType fallbackIcon,
  );
}
