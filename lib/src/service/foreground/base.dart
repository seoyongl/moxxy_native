import 'dart:io';
import 'package:moxlib/moxlib.dart';
import 'package:moxxy_native/src/service/config.dart';
import 'package:moxxy_native/src/service/datasender/types.dart';
import 'package:moxxy_native/src/service/exceptions.dart';
import 'package:moxxy_native/src/service/foreground/isolate.dart';
import 'package:moxxy_native/src/service/foreground/pigeon.dart';

/// Wrapper API that is only available to the UI isolate.
// TODO(Unknown): Dumb naming. Name it something better
abstract class ForegroundService {
  /// Perform setup such that we [handleData] is called whenever the background service
  /// sends data to the foreground.
  Future<void> attach(HandleEventCallback handleData);

  /// Start the background service with the config [config]. Additionally, perform
  /// setup such that [uiHandleData] is called whenever the background service sends
  /// data to the foreground.
  Future<void> start(ServiceConfig config, HandleEventCallback uiHandleData);

  /// Return true if the background service is running. False, if not.
  Future<bool> isRunning();

  /// Return the [AwaitableDataSender] that is used to send data to the background service.
  ForegroundServiceDataSender getDataSender();

  /// Convenience wrapper around getDataSender().sendData. The arguments are the same
  /// as for [AwaitableDataSender].
  Future<BackgroundEvent?> send(
    BackgroundCommand command, {
    bool awaitable = true,
  });
}

/// "Singleton" ForegroundService instance to prevent having to type "GetIt.I.get<ForegroundService>()"
ForegroundService? _service;

/// Either returns or creates a [ForegroundService] object of the correct type for the
/// current platform.
ForegroundService getForegroundService() {
  if (_service == null) {
    if (Platform.isAndroid) {
      _service = PigeonForegroundService();
    } else if (Platform.isLinux || Platform.isWindows || Platform.isMacOS) {
      _service = IsolateForegroundService();
    } else {
      throw UnsupportedPlatformException();
    }
  }

  return _service!;
}
