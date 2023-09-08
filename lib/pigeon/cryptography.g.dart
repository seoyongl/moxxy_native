// Autogenerated from Pigeon (v11.0.1), do not edit directly.
// See also: https://pub.dev/packages/pigeon
// ignore_for_file: public_member_api_docs, non_constant_identifier_names, avoid_as, unused_import, unnecessary_parenthesis, prefer_null_aware_operators, omit_local_variable_types, unused_shown_name, unnecessary_import

import 'dart:async';
import 'dart:typed_data' show Float64List, Int32List, Int64List, Uint8List;

import 'package:flutter/foundation.dart' show ReadBuffer, WriteBuffer;
import 'package:flutter/services.dart';

enum CipherAlgorithm {
  aes128GcmNoPadding,
  aes256GcmNoPadding,
  aes256CbcPkcs7,
}

class CryptographyResult {
  CryptographyResult({
    required this.plaintextHash,
    required this.ciphertextHash,
  });

  Uint8List plaintextHash;

  Uint8List ciphertextHash;

  Object encode() {
    return <Object?>[
      plaintextHash,
      ciphertextHash,
    ];
  }

  static CryptographyResult decode(Object result) {
    result as List<Object?>;
    return CryptographyResult(
      plaintextHash: result[0]! as Uint8List,
      ciphertextHash: result[1]! as Uint8List,
    );
  }
}

class _MoxxyCryptographyApiCodec extends StandardMessageCodec {
  const _MoxxyCryptographyApiCodec();
  @override
  void writeValue(WriteBuffer buffer, Object? value) {
    if (value is CryptographyResult) {
      buffer.putUint8(128);
      writeValue(buffer, value.encode());
    } else {
      super.writeValue(buffer, value);
    }
  }

  @override
  Object? readValueOfType(int type, ReadBuffer buffer) {
    switch (type) {
      case 128:
        return CryptographyResult.decode(readValue(buffer)!);
      default:
        return super.readValueOfType(type, buffer);
    }
  }
}

class MoxxyCryptographyApi {
  /// Constructor for [MoxxyCryptographyApi].  The [binaryMessenger] named argument is
  /// available for dependency injection.  If it is left null, the default
  /// BinaryMessenger will be used which routes to the host platform.
  MoxxyCryptographyApi({BinaryMessenger? binaryMessenger})
      : _binaryMessenger = binaryMessenger;
  final BinaryMessenger? _binaryMessenger;

  static const MessageCodec<Object?> codec = _MoxxyCryptographyApiCodec();

  Future<CryptographyResult?> encryptFile(
      String arg_sourcePath,
      String arg_destPath,
      Uint8List arg_key,
      Uint8List arg_iv,
      CipherAlgorithm arg_algorithm,
      String arg_hashSpec) async {
    final BasicMessageChannel<Object?> channel = BasicMessageChannel<Object?>(
        'dev.flutter.pigeon.moxxy_native.MoxxyCryptographyApi.encryptFile',
        codec,
        binaryMessenger: _binaryMessenger);
    final List<Object?>? replyList = await channel.send(<Object?>[
      arg_sourcePath,
      arg_destPath,
      arg_key,
      arg_iv,
      arg_algorithm.index,
      arg_hashSpec
    ]) as List<Object?>?;
    if (replyList == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
      );
    } else if (replyList.length > 1) {
      throw PlatformException(
        code: replyList[0]! as String,
        message: replyList[1] as String?,
        details: replyList[2],
      );
    } else {
      return (replyList[0] as CryptographyResult?);
    }
  }

  Future<CryptographyResult?> decryptFile(
      String arg_sourcePath,
      String arg_destPath,
      Uint8List arg_key,
      Uint8List arg_iv,
      CipherAlgorithm arg_algorithm,
      String arg_hashSpec) async {
    final BasicMessageChannel<Object?> channel = BasicMessageChannel<Object?>(
        'dev.flutter.pigeon.moxxy_native.MoxxyCryptographyApi.decryptFile',
        codec,
        binaryMessenger: _binaryMessenger);
    final List<Object?>? replyList = await channel.send(<Object?>[
      arg_sourcePath,
      arg_destPath,
      arg_key,
      arg_iv,
      arg_algorithm.index,
      arg_hashSpec
    ]) as List<Object?>?;
    if (replyList == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
      );
    } else if (replyList.length > 1) {
      throw PlatformException(
        code: replyList[0]! as String,
        message: replyList[1] as String?,
        details: replyList[2],
      );
    } else {
      return (replyList[0] as CryptographyResult?);
    }
  }

  Future<Uint8List?> hashFile(
      String arg_sourcePath, String arg_hashSpec) async {
    final BasicMessageChannel<Object?> channel = BasicMessageChannel<Object?>(
        'dev.flutter.pigeon.moxxy_native.MoxxyCryptographyApi.hashFile', codec,
        binaryMessenger: _binaryMessenger);
    final List<Object?>? replyList = await channel
        .send(<Object?>[arg_sourcePath, arg_hashSpec]) as List<Object?>?;
    if (replyList == null) {
      throw PlatformException(
        code: 'channel-error',
        message: 'Unable to establish connection on channel.',
      );
    } else if (replyList.length > 1) {
      throw PlatformException(
        code: replyList[0]! as String,
        message: replyList[1] as String?,
        details: replyList[2],
      );
    } else {
      return (replyList[0] as Uint8List?);
    }
  }
}
