import 'package:flutter/widgets.dart';

class AppLocalizations {
  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const localizationsDelegates = <LocalizationsDelegate<dynamic>>[];

  static const supportedLocales = <Locale>[
    Locale('en'),
    Locale('ja'),
  ];

  String get helloWorld => 'Hello, world';

  String get welcomeMessage => 'Welcome to the localization sandbox';

  String get cartItemCount => 'You have 3 items in your cart';

  String get settingsTitle => 'Settings';
}
