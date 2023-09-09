import 'dart:io';
import 'dart:typed_data';
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:moxxy_native/moxxy_native.dart';
import 'package:permission_handler/permission_handler.dart';

@pragma('vm:entry-point')
Future<void> entrypoint() async {
  WidgetsFlutterBinding.ensureInitialized();

  print('CALLED FROM NEW FLUTTERENGINE');
  final api = MoxxyBackgroundServiceApi();
  final extra = await api.getExtraData();
  print('EXTRA DATA: $extra');

  MethodChannel('org.moxxy.moxxy_native/background').setMethodCallHandler((call) async {
    print('[BG] Received ${call.method} with ${call.arguments}');
  });

  print('Waiting...');
  await Future<void>.delayed(const Duration(seconds: 5));

  await api.sendData('Hello from the foreground service');
  print('Data sent');
}

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  MyAppState createState() => MyAppState();
}

class MyAppState extends State<MyApp> {
  String? imagePath;

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
                // ignore: avoid_print
                print('User picked: $result');
              },
              child: const Text('Photo picker'),
            ),
            TextButton(
              onPressed: () async {
                final result = await MoxxyPickerApi()
                    .pickFiles(FilePickerType.imageAndVideo, true);
                // ignore: avoid_print
                print('User picked: $result');
              },
              child: const Text('Photo/Video multi-picker'),
            ),
            TextButton(
              onPressed: () async {
                final result = await MoxxyPickerApi()
                    .pickFiles(FilePickerType.generic, true);
                // ignore: avoid_print
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
                  // ignore: avoid_print
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
                  // ignore: avoid_print
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

                  final handle = PluginUtilities.getCallbackHandle(entrypoint)!
                      .toRawHandle();
                  final api = MoxxyServiceApi();
                  await api.configure(handle, 'lol');
                  MethodChannel("org.moxxy.moxxy_native/foreground").setMethodCallHandler((call) async {
                    print('[FG] Received ${call.method} with ${call.arguments}');
                    await api.sendData('Hello from the foreground');
                  });
                  await api.start();
                },
                child: const Text('Start foreground service')),
          ],
        ),
      ),
    );
  }
}
