// ignore_for_file: avoid_print
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get_it/get_it.dart';
import 'package:moxxy_native/moxxy_native.dart';
import 'package:path/path.dart' as p;
import 'package:permission_handler/permission_handler.dart';

@pragma('vm:entrypoint')
Future<void> serviceHandleData(Map<String, dynamic>? data) async {
  print('[BG] Received data $data');
  GetIt.I.get<BackgroundService>().send(
        TestEvent(),
        id: data!['id']! as String,
      );
}

@pragma('vm:entry-point')
Future<void> serviceEntrypoint(String initialLocale) async {
  // avoid_print
  print('Initial locale: $initialLocale');
}

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  MyAppState createState() => MyAppState();
}

class TestCommand extends BackgroundCommand {
  @override
  Map<String, dynamic> toJson() => {
        'request': 'return_name',
      };
}

class TestEvent extends BackgroundEvent {
  @override
  Map<String, dynamic> toJson() => {
        'name': 'Moxxy',
      };
}

class MyAppState extends State<MyApp> {
  String? imagePath;

  @override
  void initState() {
    super.initState();

    const EventChannel('org.moxxy.moxxyv2/notification_stream')
        .receiveBroadcastStream()
        .listen(
      (event) {
        print('Keyboard height: ${event as double}');
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: ListView(
          children: [
            TextButton(
              onPressed: () async {
                final result = await MoxxyPickerApi()
                    .pickFiles(FilePickerType.image, false);
                print('User picked: $result');
              },
              child: const Text('Photo picker'),
            ),
            TextButton(
              onPressed: () async {
                final result = await MoxxyPickerApi()
                    .pickFiles(FilePickerType.imageAndVideo, true);
                print('User picked: $result');
              },
              child: const Text('Photo/Video multi-picker'),
            ),
            TextButton(
              onPressed: () async {
                final result = await MoxxyPickerApi()
                    .pickFiles(FilePickerType.generic, true);
                print('User picked: $result');
              },
              child: const Text('Generic multi-picker'),
            ),
            TextButton(
              onPressed: () async {
                final result = await MoxxyPickerApi()
                    .pickFiles(FilePickerType.image, false);
                if (result.isEmpty) return;

                final encDest = '${result.first!}.enc';
                final decDest = '${result.first!}.dec';
                final encResult = await MoxxyCryptographyApi().encryptFile(
                  result.first!,
                  encDest,
                  Uint8List.fromList(List.filled(32, 1)),
                  Uint8List.fromList(List.filled(16, 2)),
                  CipherAlgorithm.aes256CbcPkcs7,
                  'SHA-256',
                );
                if (encResult == null) {
                  print('Failed to encrypt file');
                  return;
                }

                final decResult = await MoxxyCryptographyApi().decryptFile(
                  encDest,
                  decDest,
                  Uint8List.fromList(List.filled(32, 1)),
                  Uint8List.fromList(List.filled(16, 2)),
                  CipherAlgorithm.aes256CbcPkcs7,
                  'SHA-256',
                );
                if (decResult == null) {
                  print('Failed to decrypt file');
                  return;
                }

                setState(() {
                  imagePath = decDest;
                });
              },
              child: const Text('Test cryptography'),
            ),
            if (imagePath != null) Image.file(File(imagePath!)),
            TextButton(
              onPressed: () async {
                // Create channel
                if (Platform.isAndroid) {
                  await MoxxyNotificationsApi().createNotificationChannels(
                    [
                      NotificationChannel(
                        id: 'foreground_service',
                        title: 'Foreground service',
                        description: 'lol',
                        importance: NotificationChannelImportance.MIN,
                        showBadge: false,
                        vibration: false,
                        enableLights: false,
                      ),
                    ],
                  );

                  await Permission.notification.request();
                }

                final srv = getForegroundService();
                await srv.start(
                  const ServiceConfig(
                    serviceEntrypoint,
                    serviceHandleData,
                    'en',
                  ),
                  (data) async {
                    print('[FG] Received data $data');
                  },
                );

                await Future<void>.delayed(const Duration(milliseconds: 600));
                await getForegroundService().send(
                  TestCommand(),
                  awaitable: false,
                );
              },
              child: const Text('Start foreground service'),
            ),
            TextButton(
              onPressed: () async {
                // Pick a file and copy it into the internal storage directory
                final mediaDir = Directory(
                  p.join(
                    await MoxxyPlatformApi().getPersistentDataPath(),
                    'media',
                  ),
                );
                if (!mediaDir.existsSync()) {
                  await mediaDir.create(recursive: true);
                }
                final pickResult = await MoxxyPickerApi()
                    .pickFiles(FilePickerType.image, true);
                if (pickResult.isEmpty) return;

                final shareItems = List<ShareItem>.empty(growable: true);
                for (final result in pickResult) {
                  final mediaDirPath = p.join(
                    mediaDir.path,
                    p.basename(result!),
                  );
                  await File(result).copy(mediaDirPath);

                  shareItems.add(
                    ShareItem(
                      path: mediaDirPath,
                      mime: 'image/jpeg',
                    ),
                  );
                }

                // Share with the system
                await MoxxyPlatformApi().shareItems(
                  shareItems,
                  'image/*',
                );
              },
              child: const Text('Share internal files'),
            ),
            TextButton(
              onPressed: () async {
                // Share with the system
                await MoxxyPlatformApi().shareItems(
                  [
                    ShareItem(
                      mime: 'text/plain',
                      text: 'Hello World!',
                    ),
                  ],
                  'text/*',
                );
              },
              child: const Text('Share some text'),
            ),
            const TextField(),
          ],
        ),
      ),
    );
  }
}
