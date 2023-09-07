import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:moxxy_native/moxxy_native.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

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
                final result = await MoxxyPickerApi().pickFiles(FilePickerType.image, false);
                // ignore: avoid_print
                print('User picked: $result');
              },
              child: const Text('Photo picker'),
            ),
            TextButton(
              onPressed: () async {
                final result = await MoxxyPickerApi().pickFiles(FilePickerType.imageAndVideo, true);
                // ignore: avoid_print
                print('User picked: $result');
              },
              child: const Text('Photo/Video multi-picker'),
            ),
            TextButton(
              onPressed: () async {
                final result = await MoxxyPickerApi().pickFiles(FilePickerType.generic, true);
                // ignore: avoid_print
                print('User picked: $result');
              },
              child: const Text('Generic multi-picker'),
            ),
          ],
        ),
      ),
    );
  }
}
