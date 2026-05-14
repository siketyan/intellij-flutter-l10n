# Flutter L10n Sandbox

This sample project is for manually testing the IntelliJ Flutter Localizations plugin.

1. Run `./gradlew runIde` from the repository root.
2. In the sandbox IDE, open this directory as a project:
   `sandbox/flutter_l10n_sample`
3. Open `lib/main.dart`.
4. Check that `AppLocalizations.of(context)!.helloWorld`, `welcomeMessage`, and `cartItemCount`
   show folding placeholders from `lib/l10n/app_en.arb`.

The project includes generated-localization stubs so the Dart file can be inspected without running
`flutter gen-l10n`.
