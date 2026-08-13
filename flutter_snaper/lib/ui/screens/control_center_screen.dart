import 'package:flutter/material.dart';
import 'package:snaper_ai_assistant/ui/glass/glass_components.dart';

class ControlCenterScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color(0xFF0F0F1A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text("Control Center", style: TextStyle(fontWeight: FontWeight.bold)),
        centerTitle: true,
      ),
      body: GridView.count(
        padding: EdgeInsets.all(16),
        crossAxisCount: 2,
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        children: [
          _buildControlTile(Icons.smart_toy, "Multi-AI Hub", "Switch Models"),
          _buildControlTile(Icons.home, "Smart Home", "Devices & Scenes"),
          _buildControlTile(Icons.security, "Security", "Face & Voice"),
          _buildControlTile(Icons.auto_awesome, "Avatar", "Change Looks"),
          _buildControlTile(Icons.build, "AI Tools", "Notes & Gen"),
          _buildControlTile(Icons.history, "History", "Call Logs"),
        ],
      ),
    );
  }

  Widget _buildControlTile(IconData icon, String title, String sub) {
    return GlassCard(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, size: 32, color: Colors.blueAccent),
          SizedBox(height: 12),
          Text(title, style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          SizedBox(height: 4),
          Text(sub, style: TextStyle(fontSize: 12, color: Colors.white60), textAlign: TextAlign.center),
        ],
      ),
    );
  }
}
