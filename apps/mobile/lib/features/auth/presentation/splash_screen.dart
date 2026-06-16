import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 24, 20, 28),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 12),
              const Align(
                alignment: Alignment.centerRight,
                child: _RoseSymbol(),
              ),
              const Spacer(flex: 2),
              Text(
                '플러팅지옥',
                style: Theme.of(context).textTheme.headlineLarge?.copyWith(
                      fontSize: 40,
                      letterSpacing: -1.8,
                    ),
              ),
              const SizedBox(height: 12),
              Text(
                '대화나 상황을 붙여넣고, 지금\n필요한 답장과 전략을 확인하세요.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
              const Spacer(flex: 3),
              FilledButton(
                onPressed: () => context.go('/auth'),
                child: const Text('플러팅지옥 시작'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Minimal rose accent symbol: an open ring drawn in the accent color, matching
/// splash mockup 00 (장식 최소화, rose accent 원형 심볼만 사용).
class _RoseSymbol extends StatelessWidget {
  const _RoseSymbol();

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      size: const Size(64, 64),
      painter: _RoseRingPainter(
        color: Theme.of(context).colorScheme.primary,
      ),
    );
  }
}

class _RoseRingPainter extends CustomPainter {
  const _RoseRingPainter({required this.color});

  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 5
      ..strokeCap = StrokeCap.round;

    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2 - paint.strokeWidth / 2;

    // Draw a ~300° arc, leaving a small gap so it reads as an open ring.
    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      -1.2, // start angle (radians)
      5.0, // sweep angle (~286°)
      false,
      paint,
    );
  }

  @override
  bool shouldRepaint(_RoseRingPainter oldDelegate) =>
      oldDelegate.color != color;
}
