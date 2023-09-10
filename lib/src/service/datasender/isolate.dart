import 'dart:convert';
import 'dart:isolate';
import 'package:moxlib/moxlib.dart';
import 'package:moxxy_native/src/service/datasender/types.dart';

class IsolateForegroundServiceDataSender
    extends AwaitableDataSender<BackgroundCommand, BackgroundEvent> {
  IsolateForegroundServiceDataSender(this._port);
  final SendPort _port;

  @override
  Future<void> sendDataImpl(DataWrapper<JsonImplementation> data) async {
    _port.send(jsonEncode(data.toJson()));
  }
}
