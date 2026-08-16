import 'package:flutter/material.dart';
import 'dart:ui';
import 'dart:math' as math;

/// GlassCard with state-of-the-art 3D Glassmorphism, realistic diagonal reflection sweep,
/// dynamic ambient lighting glow, smooth orbiting particles, and interactive touch tilt response.
class GlassCard extends StatefulWidget {
  final Widget child;
  final VoidCallback? onClick;
  final EdgeInsetsGeometry padding;
  final Color? borderColor;
  final Color? glowColor;

  const GlassCard({
    required this.child,
    this.onClick,
    this.padding = const EdgeInsets.all(16.0),
    this.borderColor,
    this.glowColor,
  });

  @override
  _GlassCardState createState() => _GlassCardState();
}

class _GlassCardState extends State<GlassCard> with TickerProviderStateMixin {
  late AnimationController _reflectionController;
  late AnimationController _particleController;
  late AnimationController _glowController;
  
  double _tiltX = 0.0;
  double _tiltY = 0.0;
  double _scale = 1.0;

  @override
  void initState() {
    super.initState();
    
    // 1. Realistic Specular Glare Reflection Sweep controller
    _reflectionController = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: 3500),
    )..repeat();

    // 2. Smooth Particle Rotation/Orbiting controller
    _particleController = AnimationController(
      vsync: this,
      duration: Duration(seconds: 10),
    )..repeat();

    // 3. Dynamic Ambient Lighting Glow (pulsating glow pulse)
    _glowController = AnimationController(
      vsync: this,
      duration: Duration(seconds: 2),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _reflectionController.dispose();
    _particleController.dispose();
    _glowController.dispose();
    super.dispose();
  }

  void _onPointerMove(PointerEvent event, BoxConstraints constraints) {
    // Determine center of card
    final centerX = constraints.maxWidth / 2;
    final centerY = constraints.maxHeight / 2;
    
    // Relative coordinates scaled to maximum ~12 degrees of tilt
    setState(() {
      _tiltY = ((event.localPosition.dx - centerX) / centerX) * 0.15; // rotate around Y axis
      _tiltX = -((event.localPosition.dy - centerY) / centerY) * 0.15; // rotate around X axis
    });
  }

  void _onPointerDown() {
    setState(() {
      _scale = 0.95;
    });
  }

  void _onPointerUp() {
    setState(() {
      _scale = 1.0;
      _tiltX = 0.0;
      _tiltY = 0.0;
    });
  }

  @override
  Widget build(BuildContext context) {
    final activeGlowColor = widget.glowColor ?? Colors.blueAccent;

    return LayoutBuilder(
      builder: (context, constraints) {
        return MouseRegion(
          onHover: (event) => _onPointerMove(event, constraints),
          onExit: (_) => _onPointerUp(),
          child: Listener(
            onPointerDown: (_) => _onPointerDown(),
            onPointerUp: (_) => _onPointerUp(),
            onPointerCancel: (_) => _onPointerUp(),
            child: AnimatedScale(
              scale: _scale,
              duration: Duration(milliseconds: 150),
              curve: Curves.easeOutBack,
              child: Transform(
                transform: Matrix4.identity()
                  ..setEntry(3, 2, 0.0015) // perspective distortion
                  ..rotateX(_tiltX)
                  ..rotateY(_tiltY),
                alignment: Alignment.center,
                child: AnimatedBuilder(
                  animation: Listenable.merge([_glowController, _reflectionController, _particleController]),
                  builder: (context, child) {
                    final reflectionProgress = _reflectionController.value;
                    final particleValue = _particleController.value;
                    final glowValue = _glowController.value;

                    return Container(
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(24),
                        // Dynamic Ambient Lighting Glow behind the Glass
                        boxShadow: [
                          BoxShadow(
                            color: activeGlowColor.withOpacity(0.20 + (0.15 * glowValue)),
                            blurRadius: 25 + (10 * glowValue),
                            spreadRadius: -2 + (2 * glowValue),
                            offset: Offset(0, 8),
                          ),
                          BoxShadow(
                            color: Colors.black.withOpacity(0.35),
                            blurRadius: 18,
                            spreadRadius: -2,
                            offset: Offset(0, 10),
                          ),
                        ],
                      ),
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(24),
                        child: BackdropFilter(
                          filter: ImageFilter.blur(sigmaX: 18, sigmaY: 18),
                          child: Container(
                            padding: widget.padding,
                            decoration: BoxDecoration(
                              color: Colors.white.withOpacity(0.06),
                              borderRadius: BorderRadius.circular(24),
                              border: Border.all(
                                color: widget.borderColor ?? Colors.white.withOpacity(0.12),
                                width: 1.5,
                              ),
                              gradient: LinearGradient(
                                begin: Alignment.topLeft,
                                end: Alignment.bottomRight,
                                colors: [
                                  Colors.white.withOpacity(0.08),
                                  Colors.white.withOpacity(0.02),
                                ],
                              ),
                            ),
                            child: Stack(
                              clipBehavior: Clip.none,
                              children: [
                                // Smooth Rotating Particles (glowing celestial bubbles)
                                Positioned.fill(
                                  child: CustomPaint(
                                    painter: _GlassParticlesPainter(
                                      progress: particleValue,
                                      accentColor: activeGlowColor,
                                    ),
                                  ),
                                ),

                                // Specular Glare / 3D Reflection Diagonal Sweep Overlay
                                Positioned.fill(
                                  child: ShaderMask(
                                    blendMode: BlendMode.srcAtop,
                                    shaderCallback: (bounds) {
                                      final sweepOffset = -1.5 + (3.0 * reflectionProgress);
                                      return LinearGradient(
                                        begin: Alignment(sweepOffset - 0.5, -1.0),
                                        end: Alignment(sweepOffset + 0.5, 1.0),
                                        colors: [
                                          Colors.transparent,
                                          Colors.white.withOpacity(0.0),
                                          Colors.white.withOpacity(0.18),
                                          Colors.white.withOpacity(0.35),
                                          Colors.white.withOpacity(0.18),
                                          Colors.white.withOpacity(0.0),
                                          Colors.transparent,
                                        ],
                                        stops: const [0.0, 0.2, 0.4, 0.5, 0.6, 0.8, 1.0],
                                      ).createShader(bounds);
                                    },
                                    child: Container(color: Colors.transparent),
                                  ),
                                ),

                                // Main card content
                                InkWell(
                                  onTap: widget.onClick,
                                  splashColor: Colors.white10,
                                  highlightColor: Colors.white10,
                                  borderRadius: BorderRadius.circular(24),
                                  child: widget.child,
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}

/// Custom painter to draw beautiful rotating or drifting particles behind the glass layer
class _GlassParticlesPainter extends CustomPainter {
  final double progress;
  final Color accentColor;

  _GlassParticlesPainter({required this.progress, required this.accentColor});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final paint1 = Paint()..color = Colors.white.withOpacity(0.15);
    final paint2 = Paint()..color = accentColor.withOpacity(0.25);
    final paint3 = Paint()..color = Colors.white.withOpacity(0.10);

    // Orbiting Bubble 1
    final angle1 = progress * 2.0 * math.pi;
    final r1 = size.width * 0.25;
    final p1 = Offset(
      center.dx + r1 * math.cos(angle1),
      center.dy + r1 * math.sin(angle1),
    );
    canvas.drawCircle(p1, 10, paint1);

    // Orbiting Bubble 2 (opposite direction + speed)
    final angle2 = -progress * 3.0 * math.pi;
    final r2 = size.width * 0.35;
    final p2 = Offset(
      center.dx + r2 * math.cos(angle2),
      center.dy + r2 * math.sin(angle2),
    );
    canvas.drawCircle(p2, 7, paint2);

    // Drifting Bubble 3 (sine wave drift)
    final driftY = math.sin(progress * 2.0 * math.pi) * 12;
    final p3 = Offset(center.dx - size.width * 0.15, center.dy + driftY);
    canvas.drawCircle(p3, 14, paint3);
  }

  @override
  bool shouldRepaint(covariant _GlassParticlesPainter oldDelegate) {
    return oldDelegate.progress != progress || oldDelegate.accentColor != accentColor;
  }
}

class GlassIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onClick;

  const GlassIconButton({required this.icon, required this.onClick});

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: Container(
        padding: EdgeInsets.all(10),
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: Colors.white.withOpacity(0.08),
          border: Border.all(color: Colors.white.withOpacity(0.15)),
        ),
        child: Icon(icon, color: Colors.white),
      ),
      onPressed: onClick,
    );
  }
}

class GlossyButton extends StatefulWidget {
  final String label;
  final IconData? icon;
  final VoidCallback onPressed;
  final List<Color> gradientColors;

  const GlossyButton({
    required this.label,
    required this.onPressed,
    this.icon,
    this.gradientColors = const [Color(0xFFFF9F43), Color(0xFFFF5252)],
  });

  @override
  _GlossyButtonState createState() => _GlossyButtonState();
}

class _GlossyButtonState extends State<GlossyButton> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  bool _isHovered = false;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: 150),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      child: ScaleTransition(
        scale: Tween<double>(begin: 1.0, end: 0.96).animate(
          CurvedAnimation(parent: _controller, curve: Curves.easeIn),
        ),
        child: GestureDetector(
          onTapDown: (_) => _controller.forward(),
          onTapUp: (_) {
            _controller.reverse();
            widget.onPressed();
          },
          onTapCancel: () => _controller.reverse(),
          child: Container(
            padding: EdgeInsets.symmetric(horizontal: 24, vertical: 14),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(18),
              boxShadow: [
                BoxShadow(
                  color: widget.gradientColors.first.withOpacity(0.4),
                  blurRadius: _isHovered ? 20 : 12,
                  spreadRadius: _isHovered ? 3 : 1,
                  offset: Offset(0, 4),
                ),
              ],
              gradient: LinearGradient(
                colors: widget.gradientColors,
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              border: Border.all(
                color: Colors.white.withOpacity(0.35),
                width: 1.5,
              ),
            ),
            child: Stack(
              alignment: Alignment.center,
              children: [
                // Highlight Reflection
                Positioned(
                  top: 1,
                  left: 8,
                  right: 8,
                  child: Container(
                    height: 8,
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.25),
                      borderRadius: BorderRadius.vertical(top: Radius.circular(8)),
                    ),
                  ),
                ),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    if (widget.icon != null) ...[
                      Icon(widget.icon, color: Colors.white, size: 18),
                      SizedBox(width: 8),
                    ],
                    Text(
                      widget.label,
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                        letterSpacing: 0.5,
                        shadows: [
                          Shadow(
                            color: Colors.black38,
                            offset: Offset(0, 1),
                            blurRadius: 2,
                          ),
                        ],
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
  }
}
