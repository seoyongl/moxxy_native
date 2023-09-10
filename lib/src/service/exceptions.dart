import 'dart:io';

/// An exception representing that moxxy_native does not support the given platform.
class UnsupportedPlatformException implements Exception {
  UnsupportedPlatformException();

  String get message => 'Unsupported platform "${Platform.operatingSystem}"';
}
