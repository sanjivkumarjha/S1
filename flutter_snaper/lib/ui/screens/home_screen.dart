import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:snaper_ai_assistant/data/preferences/user_settings.dart';
import 'package:snaper_ai_assistant/ui/glass/glass_components.dart';
import 'package:snaper_ai_assistant/ui/screens/control_panel_screen.dart';
import 'package:flutter_animate/flutter_animate.dart';

class HomeScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final settings = Provider.of<UserSettings>(context);
    final layoutOrder = settings.homeScreenLayoutOrder.split(',');
    final mediaQuery = MediaQuery.of(context);
    final screenHeight = mediaQuery.size.height;
    final screenWidth = mediaQuery.size.width;
    final padding = mediaQuery.padding;

    return Scaffold(
      backgroundColor: Color(0xFF0F0F1A),
      body: Stack(
        children: [

          // Background Gradient - Fixed and non-scrolling
          Positioned.fill(
            child: Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F0F1A)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
            ),
          ),

          // Main Content Area
          SafeArea(











            child: LayoutBuilder(
              builder: (context, constraints) {
                return SingleChildScrollView(
                  physics: BouncingScrollPhysics(),
                  padding: EdgeInsets.symmetric(
                    horizontal: screenWidth * 0.04, // 4% of screen width
                    vertical: 10,
                  ),
                  child: ConstrainedBox(
                    constraints: BoxConstraints(minHeight: constraints.maxHeight),
                    child: Column(
                      children: [
                        // Space for Dynamic Island at the top
                        SizedBox(height: 50), 
                        
                        _buildHeader(context, settings, screenWidth),
                        SizedBox(height: 16),
                        
                        // Dynamic Layout Sections
                        ...layoutOrder.map((key) => _buildSection(context, key, settings, screenWidth)).toList(),
                        
                        // Bottom Padding for extra scroll space
                        SizedBox(height: 20),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),

          // Dynamic Island Overlay - Fixed at the top, precise touch bounds
          _buildDynamicIslandOverlay(context, padding.top),
        ],
      ),
    );
  }









  /// Precise Touch Bounds Dynamic Island
  Widget _buildDynamicIslandOverlay(BuildContext context, double topPadding) {
    return Positioned(
      top: topPadding + 6,
      left: 0,
      right: 0,
      child: Align(
        alignment: Alignment.topCenter,
        child: GestureDetector(
          onTap: () {
            // Precise touch handling only on the island itself
            print("Dynamic Island Tapped");
          },
          child: Container(
            width: 130, // Compact width
            height: 38, // Compact height
            decoration: BoxDecoration(
              color: Colors.black,
              borderRadius: BorderRadius.circular(22),
              boxShadow: [
                BoxShadow(
                  color: Colors.black45,
                  blurRadius: 10,
                  spreadRadius: 2,
                )
              ],
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.lens, size: 8, color: Colors.blueAccent).animate(onPlay: (c) => c.repeat()).fade(duration: 1.seconds),
                SizedBox(width: 8),
                Container(width: 2, height: 12, color: Colors.white24),
                SizedBox(width: 8),
                Icon(Icons.mic_none, size: 14, color: Colors.white54),
              ],
            ),
          ).animate().scale(duration: 400.ms, curve: Curves.elasticOut),
        ),

      ),
    );
  }


  Widget _buildHeader(BuildContext context, UserSettings settings, double width) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [














        Expanded(
          child: Row(
            children: [
              Container(
                width: width * 0.12 > 50 ? 50 : width * 0.12,
                height: width * 0.12 > 50 ? 50 : width * 0.12,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.blueAccent.withOpacity(0.15),
                ),



                child: Icon(Icons.bolt, color: Colors.blueAccent, size: 28),
              ),
              SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "${settings.getLocalizedText("greeting")} ${settings.ownerName}! ✨",
                      style: TextStyle(
                        color: Colors.white, 
                        fontSize: width < 360 ? 16 : 19, 
                        fontWeight: FontWeight.bold
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                    Text(
                      "Digital Assistant System",
                      style: TextStyle(color: Colors.white70, fontSize: 11),
                    ),
                  ],
                ),



              ),
            ],
          ),
        ),
        GlassIconButton(
          icon: Icons.settings,
          onClick: () {
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => ControlPanelScreen()),
            );
          },
        ),
      ],
    );
  }


  Widget _buildSection(BuildContext context, String key, UserSettings settings, double width) {
    switch (key) {
      case "RADHE_WIDGET":
        return Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: GlassCard(
            child: InkWell(
              onTap: () {
                settings.incrementRadhaJap();
              },
              child: Container(
                width: double.infinity,
                child: Column(
                  children: [
                    Text(
                      "जय श्री राधे कृष्ण (Jap Counter)",
                      style: TextStyle(
                        color: Colors.orangeAccent, 
                        fontSize: 16, 
                        fontWeight: FontWeight.bold,
                        letterSpacing: 1.1
                      ),
                    ),
                    SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.brightness_7, color: Colors.orangeAccent, size: 20),
                        SizedBox(width: 8),
                        Text(
                          "Count: ${settings.radhaJapCount}",
                          style: TextStyle(
                            color: Colors.white, 
                            fontSize: 18, 
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      case "HERO_ASSISTANT":
        return Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: GlassCard(
            child: Column(
              children: [
                Container(


                  height: width * 0.45, // Responsive size based on width
                  child: Icon(Icons.face_retouching_natural, size: width * 0.25, color: Colors.blueAccent),
                ).animate(onPlay: (controller) => controller.repeat())

                 .shimmer(duration: 2.seconds, color: Colors.white24)
                 .moveY(begin: -5, end: 5, duration: 2.seconds, curve: Curves.easeInOut),
                
                SizedBox(height: 12),
                
                Container(

                  padding: EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                  decoration: BoxDecoration(
                    color: Colors.blueAccent.withOpacity(0.1),

                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [



                      Container(
                        width: 10, 
                        height: 10, 
                        decoration: BoxDecoration(color: Colors.greenAccent, shape: BoxShape.circle)
                      ).animate(onPlay: (c) => c.repeat()).fade(duration: 800.ms),
                      SizedBox(width: 10),
                      Flexible(
                        child: Text(
                          "Awaiting Your Voice, ${settings.ownerName}", 
                          style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w500),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                ),

                
                SizedBox(height: 20),
                
                Row(
                  children: [

                    Expanded(
                      child: _buildActionBtn(context, "Quick Chat", Icons.chat_bubble_outline, Colors.white10),
                    ),
                    SizedBox(width: 12),

                    Expanded(
                      child: _buildActionBtn(context, "Quick Voice", Icons.mic, Colors.pinkAccent.withOpacity(0.85)),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      case "QUICK_TOOLS":
        return Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: Row(
            children: [
              Expanded(child: _buildToolCard("Security", Icons.security, "Verify")),
              SizedBox(width: 10),
              Expanded(child: _buildToolCard("Tools", Icons.build, "Gen AI")),
              SizedBox(width: 10),
              Expanded(child: _buildToolCard("Logs", Icons.history, "Calls")),
            ],
          ),
        );
      default:
        return SizedBox.shrink();
    }
  }







  Widget _buildToolCard(String title, IconData icon, String sub) {
    return GlassCard(
      padding: EdgeInsets.all(12),
      child: Column(
        children: [



          Icon(icon, color: Colors.blueAccent, size: 24),
          SizedBox(height: 8),
          Text(title, style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13)),
          Text(sub, style: TextStyle(color: Colors.white60, fontSize: 10)),
        ],
      ),
    );
  }

  Widget _buildActionBtn(BuildContext context, String text, IconData icon, Color color) {
    final width = MediaQuery.of(context).size.width;
    return Container(
      height: 54,
      decoration: BoxDecoration(
        color: color, 
        borderRadius: BorderRadius.circular(18),
        boxShadow: [
          if (color != Colors.white10)
            BoxShadow(color: color.withOpacity(0.3), blurRadius: 10, offset: Offset(0, 4))
        ]
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () {},
          borderRadius: BorderRadius.circular(18),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, color: Colors.white, size: 22),
              SizedBox(width: width < 360 ? 4 : 10),
              Text(
                text, 
                style: TextStyle(
                  color: Colors.white, 
                  fontWeight: FontWeight.bold,
                  fontSize: width < 360 ? 12 : 14
                )
              ),
            ],
          ),
        ),
      ),
    );
  }
}

