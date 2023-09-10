import 'dart:convert';
import 'dart:ui';

/// A function that can act as a service entrypoint.
typedef EntrypointCallback = Future<void> Function(String initialLocale);

/// A function that can be called when data is received.
typedef HandleEventCallback = Future<void> Function(Map<String, dynamic>? data);

/// Configuration that will be passed to the service's entrypoint
class ServiceConfig {
  const ServiceConfig(
    this.entrypoint,
    this.handleData,
    this.initialLocale,
  );

  /// Reconstruct the configuration from a JSON string.
  factory ServiceConfig.fromString(String rawData) {
    final data = jsonDecode(rawData) as Map<String, dynamic>;
    return ServiceConfig(
      PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(
          data['entrypoint']! as int,
        ),
      )! as EntrypointCallback,
      PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(
          data['handleData']! as int,
        ),
      )! as HandleEventCallback,
      data['initialLocale']! as String,
    );
  }

  /// The initial locale to use.
  final String initialLocale;

  /// The entrypoint to call into.
  final EntrypointCallback entrypoint;

  /// Entry function to call when the service receives data.
  final HandleEventCallback handleData;

  @override
  String toString() {
    return jsonEncode({
      'entrypoint':
          PluginUtilities.getCallbackHandle(entrypoint)!.toRawHandle(),
      'handleData':
          PluginUtilities.getCallbackHandle(handleData)!.toRawHandle(),
      'initialLocale': initialLocale,
    });
  }
}
