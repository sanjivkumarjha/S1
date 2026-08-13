import 'package:flutter/material.dart';

class UserSettings extends ChangeNotifier {
  String ownerName = "Sanjiv Sir";
  String languageCode = "en";
  bool isAutoStartOnBootEnabled = true;
  bool homeGreetingEmojiEnabled = true;
  int homeGreetingEmojiFrequency = 5;
  String homeScreenLayoutOrder = "RADHE_WIDGET,CLOCK_WIDGET,WEATHER_WIDGET,CONTROL_BANNER,HERO_ASSISTANT,QUICK_TOOLS,ASK_SNAPER";
  bool isVoiceVerified = false;

  void updateOwnerName(String name) {
    ownerName = name;
    notifyListeners();
  }
}
