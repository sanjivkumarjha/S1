import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:snaper_ai_assistant/data/preferences/user_settings.dart';
import 'package:snaper_ai_assistant/ui/glass/glass_components.dart';
import 'package:snaper_ai_assistant/ui/screens/control_panel_screen.dart';
import 'package:snaper_ai_assistant/ui/screens/control_center_screen.dart';
import 'package:snaper_ai_assistant/ui/components/anime_avatar.dart';
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









  /// Precise Touch Bounds Dynamic Island - iPhone Superseding Design with Touch Pass-through & 3900+ Emoji support
  Widget _buildDynamicIslandOverlay(BuildContext context, double topPadding) {
    final settings = Provider.of<UserSettings>(context);
    final screenWidth = MediaQuery.of(context).size.width;
    
    // Width adapts perfectly, leaving left/right headers fully touchable. No dead overlay dead-zones!
    final double islandWidth = settings.assistantState == AssistantState.SPEAKING || settings.assistantState == AssistantState.THINKING ? 200.0 : 140.0;

    return Positioned(
      top: topPadding + 6,
      left: (screenWidth - islandWidth) / 2,
      width: islandWidth,
      child: GestureDetector(
        onTap: () {
          print("Dynamic Island Tapped: ${settings.assistantState}");
          // Cycle through AssistantState values on tap as a rich interactive touch demo
          final nextIndex = (settings.assistantState.index + 1) % AssistantState.values.length;
          settings.updateAssistantState(AssistantState.values[nextIndex]);
        },
        child: Container(
          height: 38,
          decoration: BoxDecoration(
            color: Colors.black,
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: Colors.white24, width: 1.2),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.6),
                blurRadius: 12,
                spreadRadius: 3,
              )
            ],
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Emoji display support from the full unicode range (~3900+ expressions)
              Text(
                settings.currentExpressionEmoji,
                style: TextStyle(fontSize: 15),
              ),
              SizedBox(width: 8),
              Container(width: 1.2, height: 14, color: Colors.white30),
              SizedBox(width: 8),
              Flexible(
                child: Text(
                  settings.assistantState.name,
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 0.8,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              if (settings.assistantState == AssistantState.SPEAKING) ...[
                SizedBox(width: 6),
                Icon(Icons.volume_up, size: 12, color: Colors.blueAccent),
              ] else if (settings.assistantState == AssistantState.THINKING) ...[
                SizedBox(width: 6),
                SizedBox(
                  width: 10,
                  height: 10,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.deepPurpleAccent,
                  ),
                ),
              ],
            ],
          ),
        ).animate(key: ValueKey(settings.assistantState)).scale(duration: 350.ms, curve: Curves.easeOutBack),
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
                // Anime Avatar Component reacting in real-time to assistantState & conversation
                Container(
                  height: width * 0.5, // Perfect responsive height
                  child: AnimeAvatarWidget(
                    state: settings.assistantState,
                    activeAvatar: settings.activeAvatar,
                  ),
                ),
                
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
                          settings.assistantState == AssistantState.SPEAKING 
                              ? "Snaper is Speaking..." 
                              : "Awaiting Your Voice, ${settings.ownerName}", 
                          style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w500),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                ),

                if (settings.lastResponseText.isNotEmpty) ...[
                  SizedBox(height: 12),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: Text(
                      settings.lastResponseText,
                      style: TextStyle(color: Colors.white70, fontSize: 13, fontStyle: FontStyle.italic),
                      textAlign: TextAlign.center,
                      maxLines: 3,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
                
                SizedBox(height: 20),
                
                Row(
                  children: [
                    Expanded(
                      child: GlossyButton(
                        label: "Quick Chat",
                        icon: Icons.chat_bubble_outline,
                        gradientColors: [Color(0xFF6C5CE7), Color(0xFFa29bfe)],
                        onPressed: () {
                          settings.updateAssistantState(AssistantState.HUMOR);
                          settings.updateLastResponseText("Haha, that's incredibly funny! I love chatting with you!");
                        },
                      ),
                    ),
                    SizedBox(width: 12),
                    Expanded(
                      child: GlossyButton(
                        label: "Quick Voice",
                        icon: Icons.mic,
                        gradientColors: [Color(0xFFFD79A8), Color(0xFFE84393)],
                        onPressed: () {
                          settings.updateAssistantState(AssistantState.LISTENING);
                          Future.delayed(Duration(seconds: 2), () {
                            settings.updateAssistantState(AssistantState.THINKING);
                            Future.delayed(Duration(seconds: 2), () {
                              settings.updateAssistantState(AssistantState.SPEAKING);
                              settings.updateLastResponseText("Radhe Radhe, Sanjiv Sir! I am processing your command right now.");
                              Future.delayed(Duration(seconds: 4), () {
                                settings.updateAssistantState(AssistantState.LISTENING);
                              });
                            });
                          });
                        },
                      ),
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
              Expanded(child: _buildToolCard("Security", Icons.security, "Verify", null)),
              SizedBox(width: 10),
              Expanded(
                child: _buildToolCard("Agency", Icons.business_center, "Command Hub", () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => ControlCenterScreen()),
                  );
                }),
              ),
              SizedBox(width: 10),
              Expanded(child: _buildToolCard("Logs", Icons.history, "Calls", null)),
            ],
          ),
        );
      default:
        return SizedBox.shrink();
    }
  }







  Widget _buildToolCard(String title, IconData icon, String sub, VoidCallback? onTap) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(15),
      child: GlassCard(
        padding: EdgeInsets.all(12),
        child: Column(
          children: [
            Icon(icon, color: Colors.blueAccent, size: 24),
            SizedBox(height: 8),
            Text(title, style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13)),
            Text(sub, style: TextStyle(color: Colors.white60, fontSize: 10)),
          ],
        ),
      ),
    );
  }

  Widget _buildActionBtn(BuildContext context, String text, IconData icon, Color color, UserSettings settings) {
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
          onTap: () {
            // Simulated chat / voice interactions that triggers state and avatar animations
            if (text == "Quick Voice") {
              settings.updateAssistantState(AssistantState.LISTENING);
              Future.delayed(Duration(seconds: 2), () {
                settings.updateAssistantState(AssistantState.THINKING);
                Future.delayed(Duration(seconds: 2), () {
                  settings.updateAssistantState(AssistantState.SPEAKING);
                  settings.updateLastResponseText("Radhe Radhe, Sanjiv Sir! I am processing your command right now.");
                  Future.delayed(Duration(seconds: 4), () {
                    settings.updateAssistantState(AssistantState.LISTENING);
                  });
                });
              });
            } else {
              settings.updateAssistantState(AssistantState.HUMOR);
              settings.updateLastResponseText("Haha, that's incredibly funny! I love chatting with you!");
            }
          },
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

