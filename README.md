# moxxy_native

Interactions with the system for Moxxy.

This library is the successor of moxplatform, featuring
cleaner and more maintainable code.

## Implementation Status

### Android

Everything works.

### Linux

Only creating the "background service" works. For everything else, we're waiting on
[this Flutter issue](https://github.com/flutter/flutter/issues/73740), which would allow
us to implement/stub the missing native APIs.

## License

See `./LICENSE`.

## Special Thanks

Thanks to [ekasetiawans](https://github.com/ekasetiawans) for [flutter_background_service](https://github.com/ekasetiawans/flutter_background_service), which
was essentially the blueprint for the service and background service APIs. They were reimplemented
to allow the root isolate to pass some additional data to the service, which `flutter_background_service`
did not support.
