import 'package:flutter/material.dart';

/// 콘텐츠가 살짝 떠오르며 나타나는 진입 모션. DESIGN.md Motion 기준
/// (enter easing cubic-bezier(0.16,1,0.3,1) ≈ easeOutCubic, medium 280ms).
/// 마운트 시 즉시 재생하고 한 번만 동작한다(테스트의 pumpAndSettle와 호환).
class FadeInUp extends StatefulWidget {
  const FadeInUp({
    required this.child,
    this.duration = const Duration(milliseconds: 320),
    this.offset = 12,
    super.key,
  });

  final Widget child;
  final Duration duration;
  final double offset;

  @override
  State<FadeInUp> createState() => _FadeInUpState();
}

class _FadeInUpState extends State<FadeInUp> with SingleTickerProviderStateMixin {
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: widget.duration,
  )..forward();

  late final Animation<double> _curve = CurvedAnimation(
    parent: _controller,
    curve: const Cubic(0.16, 1, 0.3, 1),
  );

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _curve,
      builder: (context, child) {
        return Opacity(
          opacity: _curve.value,
          child: Transform.translate(
            offset: Offset(0, widget.offset * (1 - _curve.value)),
            child: child,
          ),
        );
      },
      child: widget.child,
    );
  }
}
