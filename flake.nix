{
  description = "moxxy_native";
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    android-nixpkgs.url = "github:tadfisher/android-nixpkgs";
  };

  outputs = { self, nixpkgs, flake-utils, android-nixpkgs }: flake-utils.lib.eachDefaultSystem (system: let
    pkgs = import nixpkgs {
      inherit system;
      config = {
        android_sdk.accept_license = true;
        allowUnfree = true;
        
        # Fix to allow building the NDK package
        # TODO: Remove once https://github.com/tadfisher/android-nixpkgs/issues/62 is resolved
        permittedInsecurePackages = [
          "python-2.7.18.6"
        ];
      };
    };
    # Everything to make Flutter happy
    sdk = android-nixpkgs.sdk.${system} (sdkPkgs: with sdkPkgs; [
      cmdline-tools-latest
      build-tools-30-0-3
      build-tools-33-0-2
      build-tools-34-0-0
      platform-tools
      emulator
      patcher-v4
      platforms-android-28
      platforms-android-29
      platforms-android-30
      platforms-android-31
      platforms-android-33

      # For flutter_zxing
      cmake-3-18-1
      #ndk-21-4-7075529
      (ndk-21-4-7075529.overrideAttrs (old: {
         buildInputs = old.buildInputs ++ [ pkgs.python27 ];
      }))
    ]);
    lib = pkgs.lib;
    pinnedJDK = pkgs.jdk17;
    flutterVersion = pkgs.flutter;
  in {
    devShell = pkgs.mkShell {
      buildInputs = with pkgs; [
        # Android
        pinnedJDK sdk ktlint

        # Linux
        clang cmake gtk3 ninja pkg-config xz pcre2 glib

        # Flutter
        flutterVersion

        # Code hygiene
	      gitlint jq
      ];

      ANDROID_SDK_ROOT = "${sdk}/share/android-sdk";
      ANDROID_HOME = "${sdk}/share/android-sdk";
      JAVA_HOME = pinnedJDK;

      # Fix an issue with Flutter using an older version of aapt2, which does not know
      # an used parameter.
      GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdk}/share/android-sdk/build-tools/34.0.0/aapt2";
    };

    apps = {
      androidLint = let 
        script = pkgs.writeShellScript "lint-android.sh" ''
        ${pkgs.ktlint}/bin/ktlint \
          --format \
          --disabled_rules=standard:package-name \
          android/src/main/kotlin/org/moxxy/moxxy_native/
        '';
      in {
        program = "${script}";
        type = "app";
      };
    };
  });
}
