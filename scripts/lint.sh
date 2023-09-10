# Format and lint the Dart code
dart format .
flutter analyze

# Format and lint the Kotlin code
ktlint --disabled_rules=standard:package-name --format android/src/main/kotlin/org/moxxy/moxxy_native
