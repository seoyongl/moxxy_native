import 'dart:io';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:moxxy_native/moxxy_native.dart';

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
                final result = await MoxxyPickerApi().pickFiles(FilePickerType.image, false);
                if (result.isEmpty) return;

                final encDest = result.first! + '.enc';
                final decDest = result.first! + '.dec';
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
              child: Text('Test cryptography'),
            ),

            if (imagePath != null)
              Image.file(File(imagePath!)),
          ],
        ),
      ),
    );
  }
}
