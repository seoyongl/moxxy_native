import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/pigeon/cryptography.g.dart',
    kotlinOut: 'android/src/main/kotlin/org/moxxy/moxxy_native/cryptography/CryptographyApi.kt',
    kotlinOptions: KotlinOptions(
      package: 'org.moxxy.moxxy_native.cryptography',
    ),
  ),
)
enum CipherAlgorithm {
  aes128GcmNoPadding,
  aes256GcmNoPadding,
  aes256CbcPkcs7;
}

class CryptographyResult {
  const CryptographyResult(this.plaintextHash, this.ciphertextHash);
  final Uint8List plaintextHash;
  final Uint8List ciphertextHash;
}

@HostApi()
abstract class MoxxyCryptographyApi {
  @async
  CryptographyResult? encryptFile(String sourcePath, String destPath, Uint8List key, Uint8List iv, CipherAlgorithm algorithm, String hashSpec);

  @async
  CryptographyResult? decryptFile(String sourcePath, String destPath, Uint8List key, Uint8List iv, CipherAlgorithm algorithm, String hashSpec);

  @async
  Uint8List? hashFile(String sourcePath, String hashSpec);
}
