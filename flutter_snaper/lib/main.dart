import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:snaper_ai_assistant/data/preferences/user_settings.dart';
import 'package:snaper_ai_assistant/ui/screens/home_screen.dart';

void main() {
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => UserSettings()),
      ],
      child: SnaperApp(),
    ),
  );
}

class SnaperApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Snaper AI Assistant',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        primarySwatch: Colors.blue,
        useMaterial3: true,
        fontFamily: 'Roboto',
      ),
      home: HomeScreen(),
    );
  }
}
