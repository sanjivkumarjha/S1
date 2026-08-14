import 'dart:async';
import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:snaper_ai_assistant/data/preferences/user_settings.dart';

class AnimeAvatarWidget extends StatefulWidget {
  final AssistantState state;
  final String activeAvatar;

  const AnimeAvatarWidget({
    Key? key,
    required this.state,
    required this.activeAvatar,
  }) : super(key: key);

  @override
  _AnimeAvatarWidgetState createState() => _AnimeAvatarWidgetState();
}

class _AnimeAvatarWidgetState extends State<AnimeAvatarWidget> with TickerProviderStateMixin {
  late AnimationController _breathingController;
  late AnimationController _blinkingController;
  late AnimationController _talkingController;
  late AnimationController _noddingController;

  bool _isBlinking = false;
  Timer? _blinkTimer;

  @override
  void initState() {
    super.initState();

    // Breathing: Continuous subtle up/down
    _breathingController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2500),
    )..repeat(reverse: true);

    // Blinking: Periodic quick eye shut
    _blinkingController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 150),
    );
    _startBlinkLoop();

    // Talking mouth: Active when SPEAKING
    _talkingController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 120),
    );

    // Nodding: Gentle nod when listening or speaking
    _noddingController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1800),
    )..repeat(reverse: true);

    _updateStateControllers();
  }

  @override
  void didUpdateWidget(AnimeAvatarWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.state != widget.state) {
      _updateStateControllers();
    }
  }

  void _updateStateControllers() {
    if (widget.state == AssistantState.SPEAKING) {
      _talkingController.repeat(reverse: true);
    } else {
      _talkingController.stop();
      _talkingController.value = 0.0;
    }
  }

  void _startBlinkLoop() {
    _blinkTimer?.cancel();
    _blinkTimer = Timer.periodic(const Duration(seconds: 4), (timer) {
      if (widget.state != AssistantState.EMERGENCY) {
        setState(() {
          _isBlinking = true;
        });
        _blinkingController.forward().then((_) {
          _blinkingController.reverse().then((_) {
            setState(() {
              _isBlinking = false;
            });
          });
        });
      }
    });
  }

  @override
  void dispose() {
    _breathingController.dispose();
    _blinkingController.dispose();
    _talkingController.dispose();
    _noddingController.dispose();
    _blinkTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: Listenable.merge([
        _breathingController,
        _talkingController,
        _noddingController,
        _blinkingController,
      ]),
      builder: (context, child) {
        // Compute translations/scaling based on state & animations
        double nodY = 0.0;
        if (widget.state == AssistantState.SPEAKING || widget.state == AssistantState.LISTENING) {
          nodY = math.sin(_noddingController.value * math.pi) * 6.0;
        }

        double breathY = math.sin(_breathingController.value * math.pi) * 4.0;
        double mouthScale = widget.state == AssistantState.SPEAKING
            ? (0.2 + 0.8 * _talkingController.value)
            : 0.1;

        return Center(
          child: Container(
            width: 200,
            height: 200,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  color: _getGlowColor(widget.state).withOpacity(0.3),
                  blurRadius: 30,
                  spreadRadius: 5,
                )
              ],
            ),
            child: Stack(
              alignment: Alignment.center,
              children: [
                // Inner Avatar Background
                Container(
                  width: 180,
                  height: 180,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    gradient: LinearGradient(
                      colors: [
                        const Color(0xFF1F1D36),
                        _getGlowColor(widget.state).withOpacity(0.2),
                      ],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    border: Border.all(
                      color: _getGlowColor(widget.state).withOpacity(0.6),
                      width: 2.5,
                    ),
                  ),
                ),

                // Hair / Head Back
                Positioned(
                  top: 25 + breathY + nodY,
                  child: _buildHairBack(),
                ),

                // Face Base
                Positioned(
                  top: 45 + breathY + nodY,
                  child: Container(
                    width: 110,
                    height: 100,
                    decoration: BoxDecoration(
                      color: const Color(0xFFFFE0BD),
                      borderRadius: BorderRadius.circular(45),
                    ),
                  ),
                ),

                // Blush / Cheeks
                Positioned(
                  top: 95 + breathY + nodY,
                  child: _buildCheeks(),
                ),

                // Eyes & Brows
                Positioned(
                  top: 70 + breathY + nodY,
                  child: _buildEyesAndBrows(),
                ),

                // Mouth
                Positioned(
                  top: 105 + breathY + nodY,
                  child: _buildMouth(mouthScale),
                ),

                // Hair / Head Front (Bangs & Hair Strands)
                Positioned(
                  top: 20 + breathY + nodY,
                  child: _buildHairFront(),
                ),

                // Head Accessories (Ears, Ribbon or Halo)
                Positioned(
                  top: 10 + breathY + nodY,
                  child: _buildAccessories(),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Color _getGlowColor(AssistantState state) {
    switch (state) {
      case AssistantState.LISTENING:
        return Colors.tealAccent;
      case AssistantState.THINKING:
        return Colors.deepPurpleAccent;
      case AssistantState.SPEAKING:
        return Colors.blueAccent;
      case AssistantState.HUMOR:
        return Colors.amberAccent;
      case AssistantState.ANGER:
        return Colors.redAccent;
      case AssistantState.CAMERA_ACTIVE:
        return Colors.greenAccent;
      case AssistantState.TORCH:
        return Colors.yellow;
      case AssistantState.AUTOMATION:
        return Colors.orangeAccent;
      case AssistantState.EMERGENCY:
        return Colors.red;
    }
  }

  Widget _buildHairBack() {
    // Elegant flowing anime purple/blue hair back
    final hairColor = _getHairColor();
    return Container(
      width: 140,
      height: 120,
      decoration: BoxDecoration(
        color: hairColor,
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(70),
          topRight: Radius.circular(70),
          bottomLeft: Radius.circular(30),
          bottomRight: Radius.circular(30),
        ),
      ),
    );
  }

  Widget _buildCheeks() {
    bool isBlushing = widget.state == AssistantState.HUMOR ||
        widget.state == AssistantState.SPEAKING ||
        widget.state == AssistantState.LISTENING;
    return SizedBox(
      width: 100,
      height: 20,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Container(
            width: 18,
            height: 10,
            decoration: BoxDecoration(
              color: isBlushing ? Colors.pinkAccent.withOpacity(0.4) : Colors.transparent,
              borderRadius: BorderRadius.circular(10),
            ),
          ),
          Container(
            width: 18,
            height: 10,
            decoration: BoxDecoration(
              color: isBlushing ? Colors.pinkAccent.withOpacity(0.4) : Colors.transparent,
              borderRadius: BorderRadius.circular(10),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEyesAndBrows() {
    return SizedBox(
      width: 84,
      height: 40,
      child: Stack(
        children: [
          // Left Eye Group
          Positioned(
            left: 5,
            top: 5,
            child: _buildSingleEyeGroup(isLeft: true),
          ),
          // Right Eye Group
          Positioned(
            right: 5,
            top: 5,
            child: _buildSingleEyeGroup(isLeft: false),
          ),
        ],
      ),
    );
  }

  Widget _buildSingleEyeGroup({required bool isLeft}) {
    // Eye Expression based on state
    bool closed = _isBlinking ||
        widget.state == AssistantState.EMERGENCY ||
        widget.state == AssistantState.ANGER;

    if (widget.state == AssistantState.EMERGENCY) {
      // Worry / closed concern eyes
      return Column(
        children: [
          // Worried Brow
          Transform.rotate(
            angle: isLeft ? -0.15 : 0.15,
            child: Container(width: 22, height: 3, color: Colors.indigo.shade900),
          ),
          const SizedBox(height: 6),
          // Squeezed worried eyes > <
          Text(isLeft ? ">" : "<",
              style: TextStyle(
                  color: Colors.indigo.shade900, fontSize: 16, fontWeight: FontWeight.bold)),
        ],
      );
    }

    if (widget.state == AssistantState.ANGER) {
      // Angry eyes slant
      return Column(
        children: [
          // Angled angry brow
          Transform.rotate(
            angle: isLeft ? 0.25 : -0.25,
            child: Container(width: 22, height: 3.5, color: Colors.indigo.shade900),
          ),
          const SizedBox(height: 4),
          // Slanted sharp look
          Container(
            width: 24,
            height: 14,
            decoration: BoxDecoration(
              color: Colors.indigo.shade900,
              borderRadius: BorderRadius.circular(7),
            ),
            child: Align(
              alignment: Alignment.topCenter,
              child: Container(
                width: 6,
                height: 6,
                decoration: const BoxDecoration(
                  color: Colors.white,
                  shape: BoxShape.circle,
                ),
              ),
            ),
          ),
        ],
      );
    }

    if (widget.state == AssistantState.HUMOR) {
      // Laughing arc eyes ^ ^
      return Column(
        children: [
          // Excited Brow
          Transform.rotate(
            angle: isLeft ? -0.1 : 0.1,
            child: Container(width: 22, height: 3, color: Colors.indigo.shade900),
          ),
          const SizedBox(height: 6),
          Text("^",
              style: TextStyle(
                  color: Colors.indigo.shade900, fontSize: 20, fontWeight: FontWeight.bold)),
        ],
      );
    }

    if (closed) {
      // Regular blink or sleeping closed eyes
      return Column(
        children: [
          Container(width: 22, height: 3, color: Colors.indigo.shade800),
          const SizedBox(height: 8),
          Container(
            width: 24,
            height: 3,
            color: Colors.indigo.shade900,
          ),
        ],
      );
    }

    // Normal / Thinking / Listening cute wide anime eyes with sparkles
    return Column(
      children: [
        // Normal Brow
        Container(width: 22, height: 2.5, color: Colors.indigo.shade800),
        const SizedBox(height: 5),
        Container(
          width: 24,
          height: 22,
          decoration: BoxDecoration(
            color: Colors.indigo.shade900,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Stack(
            children: [
              // Sparkle 1
              Positioned(
                top: 3,
                left: isLeft ? 4 : 10,
                child: Container(
                  width: 7,
                  height: 7,
                  decoration: const BoxDecoration(
                    color: Colors.white,
                    shape: BoxShape.circle,
                  ),
                ),
              ),
              // Sparkle 2
              Positioned(
                bottom: 4,
                right: isLeft ? 4 : 10,
                child: Container(
                  width: 4,
                  height: 4,
                  decoration: const BoxDecoration(
                    color: Colors.white70,
                    shape: BoxShape.circle,
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildMouth(double mouthScale) {
    if (widget.state == AssistantState.ANGER) {
      // Angered straight/slanted line mouth
      return Container(
        width: 18,
        height: 3,
        color: Colors.indigo.shade900,
      );
    }

    if (widget.state == AssistantState.HUMOR) {
      // Big laughing open mouth
      return Container(
        width: 20,
        height: 12,
        decoration: BoxDecoration(
          color: Colors.redAccent.shade100,
          borderRadius: const BorderRadius.only(
            bottomLeft: Radius.circular(10),
            bottomRight: Radius.circular(10),
          ),
        ),
      );
    }

    if (widget.state == AssistantState.THINKING) {
      // Cute thinking little 'o' or dot mouth
      return Container(
        width: 8,
        height: 8,
        decoration: BoxDecoration(
          color: Colors.indigo.shade900,
          shape: BoxShape.circle,
        ),
      );
    }

    // Default cute talking/smiling mouth
    return Container(
      width: 16,
      height: 2 + (12 * mouthScale),
      decoration: BoxDecoration(
        color: Colors.redAccent.shade100,
        borderRadius: BorderRadius.circular(10),
      ),
    );
  }

  Widget _buildHairFront() {
    // Beautiful anime front bangs / hair strands overlapping the face
    final hairColor = _getHairColor();
    return SizedBox(
      width: 120,
      height: 45,
      child: Stack(
        children: [
          // Left Bangs
          Positioned(
            left: 0,
            top: 0,
            child: Transform.rotate(
              angle: -0.1,
              child: Container(
                width: 35,
                height: 35,
                decoration: BoxDecoration(
                  color: hairColor,
                  borderRadius: const BorderRadius.only(
                    bottomRight: Radius.circular(30),
                  ),
                ),
              ),
            ),
          ),
          // Middle Bangs
          Positioned(
            left: 45,
            top: -2,
            child: Transform.rotate(
              angle: 0.1,
              child: Container(
                width: 30,
                height: 32,
                decoration: BoxDecoration(
                  color: hairColor,
                  borderRadius: const BorderRadius.only(
                    bottomLeft: Radius.circular(15),
                    bottomRight: Radius.circular(15),
                  ),
                ),
              ),
            ),
          ),
          // Right Bangs
          Positioned(
            right: 0,
            top: 0,
            child: Transform.rotate(
              angle: 0.1,
              child: Container(
                width: 35,
                height: 35,
                decoration: BoxDecoration(
                  color: hairColor,
                  borderRadius: const BorderRadius.only(
                    bottomLeft: Radius.circular(30),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAccessories() {
    // Render ears or a beautiful pink ribbon depending on activeAvatar setting
    bool isCyber = widget.activeAvatar.contains("Cyberpunk");
    return SizedBox(
      width: 140,
      height: 30,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Left Ear Accent
          Transform.rotate(
            angle: -0.3,
            child: Container(
              width: 25,
              height: 25,
              decoration: BoxDecoration(
                color: isCyber ? Colors.cyanAccent : const Color(0xFFFF94B4),
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(20),
                  bottomRight: Radius.circular(15),
                ),
              ),
            ),
          ),
          // Right Ear Accent
          Transform.rotate(
            angle: 0.3,
            child: Container(
              width: 25,
              height: 25,
              decoration: BoxDecoration(
                color: isCyber ? Colors.cyanAccent : const Color(0xFFFF94B4),
                borderRadius: const BorderRadius.only(
                  topRight: Radius.circular(20),
                  bottomLeft: Radius.circular(15),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Color _getHairColor() {
    if (widget.activeAvatar.contains("Cyberpunk")) {
      return Colors.cyan.shade700;
    }
    if (widget.activeAvatar.contains("Friendly 2D")) {
      return Colors.amber.shade700;
    }
    if (widget.activeAvatar.contains("Spark Sparkle")) {
      return Colors.pink.shade300;
    }
    // Default: Purple/Blue Hologram Classic
    return const Color(0xFF6C63FF);
  }
}
