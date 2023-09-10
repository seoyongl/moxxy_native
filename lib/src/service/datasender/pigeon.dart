import 'dart:convert';
import 'package:moxlib/moxlib.dart';
import 'package:moxxy_native/pigeon/service.g.dart';
import 'package:moxxy_native/src/service/datasender/types.dart';

class PigeonForegroundServiceDataSender
    extends AwaitableDataSender<BackgroundCommand, BackgroundEvent> {
  PigeonForegroundServiceDataSender(this._api);
  final MoxxyServiceApi _api;

  @override
  Future<void> sendDataImpl(DataWrapper<JsonImplementation> data) {
    return _api.sendData(jsonEncode(data.toJson()));
  }
}
