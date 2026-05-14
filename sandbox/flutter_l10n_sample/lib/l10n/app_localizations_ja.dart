// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Japanese (`ja`).
class AppLocalizationsJa extends AppLocalizations {
  AppLocalizationsJa([String locale = 'ja']) : super(locale);

  @override
  String get helloWorld => 'こんにちは、世界';

  @override
  String get welcomeMessage => 'ローカライズ sandbox へようこそ';

  @override
  String get cartItemCount => 'カートに 3 件の商品があります';

  @override
  String get settingsTitle => '設定';
}
