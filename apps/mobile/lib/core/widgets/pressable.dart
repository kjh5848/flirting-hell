import 'package:flutter/material.dart';

/// 탭하면 살짝 줄어드는 press 피드백. Material 잉크가 안 닿는 커스텀/다크 면의
/// 칩·카드에 촉감을 준다(버튼은 기본 ripple로 충분하니 이걸 쓰지 않는다).
class Pressable extends StatefulWidget {
  const Pressable({
    required this.child,
    required this.onTap,
    this.scale = 0.96,
    super.key,
  });

  final Widget child;
  final VoidCallback? onTap;
  final double scale;

  @override
  State<Pressable> createState() => _PressableState();
}

class _PressableState extends State<Pressable> {
  bool _down = false;

  void _set(bool down) {
    if (widget.onTap == null) return;
    setState(() => _down = down);
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: widget.onTap,
      onTapDown: (_) => _set(true),
      onTapUp: (_) => _set(false),
      onTapCancel: () => _set(false),
      child: AnimatedScale(
        scale: _down ? widget.scale : 1,
        duration: const Duration(milliseconds: 110),
        curve: Curves.easeOut,
        child: widget.child,
      ),
    );
  }
}
