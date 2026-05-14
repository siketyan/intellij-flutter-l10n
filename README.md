# Flutter Localizations Support for IntelliJ IDEA

IntelliJ IDEA plugin for working with Flutter `gen-l10n` localizations.

The plugin reads Flutter ARB files and `l10n.yaml`, then uses that information in Dart editors to make localization references easier to inspect and navigate.

## Features

- Shows localized ARB messages as folding placeholders for Dart localization references.
- Supports direct references such as `AppLocalizations.of(context)!.settingsTitle`.
- Supports null-aware access such as `AppLocalizations.of(context)?.settingsTitle ?? ''`.
- Supports variables that store the localization instance, such as:

  ```dart
  final l10n = AppLocalizations.of(context);
  Text(l10n.settingsTitle);
  ```

- Recognizes `.arb` files as JSON so existing JSON editor support works for ARB files.
- Navigates from Dart localization keys to matching ARB entries across locales.
- Navigates from ARB keys back to Dart usages.
- Displays ARB navigation candidates using the translated text and the source file name, for example `app_en.arb`.

## Requirements

- IntelliJ IDEA 2026.1 or later.
- Dart plugin.
- JSON platform module.

The plugin ID is `jp.s6n.idea.flutter.l10n`.

## Supported Flutter Configuration

The plugin looks for `l10n.yaml` and uses these Flutter `gen-l10n` settings when present:

- `arb-dir`
- `template-arb-file`
- `output-class`
- `preferred-supported-locales`

If `l10n.yaml` is missing, it falls back to Flutter defaults:

- ARB directory: `lib/l10n`
- Template ARB file: `app_en.arb`
- Output class: `AppLocalizations`

## Development

Run the plugin in a sandbox IDE:

```shell
./gradlew runIde
```

Run tests:

```shell
./gradlew test
```

Verify plugin compatibility:

```shell
./gradlew verifyPlugin
```

Build the plugin ZIP:

```shell
./gradlew buildPlugin
```

## Manual Testing

A small Flutter localization sample is included at:

```text
sandbox/flutter_l10n_sample
```

To test manually:

1. Run `./gradlew runIde`.
2. Open `sandbox/flutter_l10n_sample` in the sandbox IDE.
3. Inspect `lib/main.dart` for folding placeholders and navigation behavior.
4. Use Go to Declaration or Go to Implementation on localization keys to jump between Dart references and ARB entries.

The sample includes generated localization Dart files and does not require running `flutter gen-l10n` before opening it.

## Installation

For local development, build the ZIP with `./gradlew buildPlugin` and install it from disk:

`Settings/Preferences` > `Plugins` > `Install Plugin from Disk...`

JetBrains Marketplace installation details can be added after the plugin is published.
