import 'package:moxxy_native/src/service/config.dart';
import 'package:moxxy_native/src/service/datasender/types.dart';

/// Wrapper API that is only available to the background service.
abstract class BackgroundService {
  /// Send [event] with optional id [id] to the foreground.
  Future<void> send(BackgroundEvent event, {String? id});

  /// Platform specific initialization routine that is called after
  /// the entrypoint has been called.
  Future<void> init(ServiceConfig config);

  /// Update the notification body, if the platform shows a persistent
  /// notification.
  void setNotificationBody(String body);
}
