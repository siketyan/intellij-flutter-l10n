import 'package:flutter/material.dart';
import 'package:flutter_l10n_sandbox/l10n/app_localizations.dart';

void main() {
  runApp(const SandboxApp());
}

class SandboxApp extends StatelessWidget {
  const SandboxApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      localizationsDelegates: AppLocalizations.localizationsDelegates,
      supportedLocales: AppLocalizations.supportedLocales,
      home: const SandboxHome(),
    );
  }
}

class SandboxHome extends StatelessWidget {
  const SandboxHome({super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(AppLocalizations.of(context)!.helloWorld),
      ),
      body: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          Text(AppLocalizations.of(context)!.welcomeMessage),
          Text(AppLocalizations.of(context)!.cartItemCount),
          Text(l10n.helloWorld),
          Text(AppLocalizations.of(context)?.settingsTitle ?? ''),
        ],
      ),
    );
  }
}
