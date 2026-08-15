import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:snaper_ai_assistant/data/preferences/user_settings.dart';
import 'package:snaper_ai_assistant/ui/glass/glass_components.dart';

/// Granular App Access Switch Selector UI
///
/// Displays installed applications with system-style blue ON/OFF toggle switches.
/// Distinguishes strictly between standard and business app variants using unique
/// bundle IDs (e.g., 'com.whatsapp' vs 'com.whatsapp.w4b').
/// All handlers wrapped in null-safe try-catch blocks to prevent background crashes.
class AppAccessSwitchScreen extends StatefulWidget {
  @override
  _AppAccessSwitchScreenState createState() => _AppAccessSwitchScreenState();
}

class _AppAccessSwitchScreenState extends State<AppAccessSwitchScreen> {
  // Predefined app list with exact package names for standard vs business variants
  final List<AppEntry> _availableApps = [
    AppEntry('WhatsApp', 'com.whatsapp', Icons.chat, Colors.green),
    AppEntry('WhatsApp Business', 'com.whatsapp.w4b', Icons.business, Colors.teal),
    AppEntry('Instagram', 'com.instagram.android', Icons.camera_alt, Colors.purple),
    AppEntry('Instagram Lite', 'com.instagram.lite', Icons.camera_alt_outlined, Colors.deepPurple),
    AppEntry('Facebook', 'com.facebook.katana', Icons.facebook, Colors.blue),
    AppEntry('Facebook Lite', 'com.facebook.lite', Icons.facebook_outlined, Colors.lightBlue),
    AppEntry('YouTube', 'com.google.android.youtube', Icons.play_circle, Colors.red),
    AppEntry('YouTube Music', 'com.google.android.apps.youtube.music', Icons.music_note, Colors.pink),
    AppEntry('Google Maps', 'com.google.android.apps.maps', Icons.map, Colors.blueGrey),
    AppEntry('Google Drive', 'com.google.android.apps.docs', Icons.drive_folder_upload, Colors.orange),
    AppEntry('Gmail', 'com.google.android.gm', Icons.email, Colors.redAccent),
    AppEntry('Google Photos', 'com.google.android.apps.photos', Icons.photo_library, Colors.yellow),
    AppEntry('Twitter / X', 'com.twitter.android', Icons.tag, Colors.blueGrey),
    AppEntry('LinkedIn', 'com.linkedin.android', Icons.work, Colors.blue),
    AppEntry('Telegram', 'org.telegram.messenger', Icons.send, Colors.cyan),
    AppEntry('Signal', 'org.thoughtcrime.securesms', Icons.lock, Colors.green),
    AppEntry('Snapchat', 'com.snapchat.android', Icons.mood, Colors.yellow),
    AppEntry('Netflix', 'com.netflix.mediaclient', Icons.movie, Colors.red),
    AppEntry('Amazon', 'com.amazon.mShop.android.shopping', Icons.shopping_cart, Colors.orange),
    AppEntry('Flipkart', 'com.flipkart.android', Icons.shopping_bag, Colors.blue),
    AppEntry('Phone / Dialer', 'com.google.android.dialer', Icons.phone, Colors.green),
    AppEntry('Messages', 'com.google.android.apps.messaging', Icons.message, Colors.blue),
    AppEntry('Camera', 'com.google.android.GoogleCamera', Icons.camera, Colors.grey),
    AppEntry('Gallery', 'com.google.android.apps.photos', Icons.photo, Colors.pink),
    AppEntry('Settings', 'com.android.settings', Icons.settings, Colors.grey),
    AppEntry('Chrome', 'com.android.chrome', Icons.language, Colors.yellow),
    AppEntry('Play Store', 'com.android.vending', Icons.store, Colors.green),
    AppEntry('System Files', 'com.android.documentsui', Icons.folder, Colors.amber),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color(0xFF0F0F1A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text(
          "App Access Switcher",
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
        ),
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: Consumer<UserSettings>(
        builder: (context, settings, child) {
          return Column(
            children: [
              // Info banner
              Container(
                margin: EdgeInsets.all(12),
                padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                decoration: BoxDecoration(
                  color: Colors.blue.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.blue.withOpacity(0.3)),
                ),
                child: Row(
                  children: [
                    Icon(Icons.info_outline, color: Colors.blueAccent, size: 20),
                    SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        "Toggle which apps the assistant can access. "
                        "Standard and Business variants are listed separately.",
                        style: TextStyle(color: Colors.white70, fontSize: 12),
                      ),
                    ),
                  ],
                ),
              ),
              // App count
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      "Installed Applications",
                      style: TextStyle(
                        color: Colors.white54,
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    Text(
                      "${_getEnabledCount(settings)} / ${_availableApps.length} enabled",
                      style: TextStyle(
                        color: Colors.blueAccent,
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ),
              // App list
              Expanded(
                child: ListView.builder(
                  padding: EdgeInsets.symmetric(horizontal: 12),
                  itemCount: _availableApps.length,
                  itemBuilder: (context, index) {
                    final app = _availableApps[index];
                    return _buildAppTile(app, settings);
                  },
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildAppTile(AppEntry app, UserSettings settings) {
    final isEnabled = settings.enabledApps[app.displayName] ?? true;

    return Container(
      margin: EdgeInsets.symmetric(vertical: 3),
      decoration: BoxDecoration(
        color: Color(0xFF1A1A2E),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isEnabled ? Colors.blue.withOpacity(0.3) : Colors.white10,
        ),
      ),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: app.color.withOpacity(0.2),
          child: Icon(app.icon, color: app.color, size: 22),
        ),
        title: Text(
          app.displayName,
          style: TextStyle(
            color: Colors.white,
            fontSize: 14,
            fontWeight: FontWeight.w500,
          ),
        ),
        subtitle: Text(
          app.packageName,
          style: TextStyle(color: Colors.white38, fontSize: 11),
        ),
        trailing: Switch(
          value: isEnabled,
          activeColor: Colors.blueAccent,
          activeTrackColor: Colors.blue.withOpacity(0.5),
          onChanged: (value) {
            try {
              settings.toggleApp(app.displayName, value);
            } catch (e) {
              // Null-safe catch to prevent crashes
              debugPrint("App toggle error: $e");
            }
          },
        ),
        onTap: () {
          try {
            settings.toggleApp(app.displayName, !isEnabled);
          } catch (e) {
            // Null-safe catch to prevent crashes
            debugPrint("App toggle error: $e");
          }
        },
      ),
    );
  }

  int _getEnabledCount(UserSettings settings) {
    try {
      return _availableApps.where((app) {
        return settings.enabledApps[app.displayName] ?? true;
      }).length;
    } catch (e) {
      return 0;
    }
  }
}

class AppEntry {
  final String displayName;
  final String packageName;
  final IconData icon;
  final Color color;

  AppEntry(this.displayName, this.packageName, this.icon, this.color);
}