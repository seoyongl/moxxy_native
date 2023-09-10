import 'package:moxlib/moxlib.dart';

typedef ForegroundServiceDataSender
    = AwaitableDataSender<BackgroundCommand, BackgroundEvent>;

abstract class BackgroundCommand implements JsonImplementation {}

abstract class BackgroundEvent implements JsonImplementation {}
