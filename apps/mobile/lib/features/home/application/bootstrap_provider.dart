import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../auth/application/auth_controller.dart';

final bootstrapProvider = Provider<BootstrapSnapshot?>((ref) {
  final auth = ref.watch(authControllerProvider);

  if (!auth.isSignedIn) {
    return null;
  }

  return const BootstrapSnapshot(
    user: BootstrapUser(
      userId: 'usr_local',
      onboardingCompleted: false,
      profile: BootstrapProfile(
        nickname: '주혁',
        speechStyle: '짧고 자연스럽게',
        datingStyle: '천천히 확인하는 편',
        guidanceLevel: 'BALANCED',
        preferredPartnerStyle: '다정하게 표현하는 사람',
        avoidAdvice: '단정하거나 압박하는 조언은 피하기',
      ),
    ),
    usage: BootstrapUsage(
      creditBalance: 0,
      freeRemaining: 3,
      rewardAdRemaining: 3,
    ),
    recentRooms: [
      BootstrapRecentRoom(
        roomId: 'room_jiwoo',
        alias: '지우',
        currentConcern: '답장은 왔는데 마음을 모르겠는 대화',
        lastTurnSummary: '상대가 집에 있다고 답했고 대화를 이어갈 여지가 있음',
      ),
      BootstrapRecentRoom(
        roomId: 'room_harin',
        alias: '하린',
        currentConcern: '약속을 자연스럽게 잡고 싶은 상황',
        lastTurnSummary: '대화 분위기는 가볍고 제안 타이밍을 조절하는 중',
      ),
    ],
  );
});

class BootstrapSnapshot {
  const BootstrapSnapshot({
    required this.user,
    required this.usage,
    required this.recentRooms,
  });

  final BootstrapUser user;
  final BootstrapUsage usage;
  final List<BootstrapRecentRoom> recentRooms;
}

class BootstrapUser {
  const BootstrapUser({
    required this.userId,
    required this.onboardingCompleted,
    required this.profile,
  });

  final String userId;
  final bool onboardingCompleted;
  final BootstrapProfile profile;
}

class BootstrapProfile {
  const BootstrapProfile({
    required this.nickname,
    required this.speechStyle,
    required this.datingStyle,
    required this.guidanceLevel,
    required this.preferredPartnerStyle,
    required this.avoidAdvice,
  });

  final String nickname;
  final String speechStyle;
  final String datingStyle;
  final String guidanceLevel;
  final String preferredPartnerStyle;
  final String avoidAdvice;
}

class BootstrapUsage {
  const BootstrapUsage({
    required this.creditBalance,
    required this.freeRemaining,
    required this.rewardAdRemaining,
  });

  final int creditBalance;
  final int freeRemaining;
  final int rewardAdRemaining;
}

class BootstrapRecentRoom {
  const BootstrapRecentRoom({
    required this.roomId,
    required this.alias,
    required this.currentConcern,
    required this.lastTurnSummary,
  });

  final String roomId;
  final String alias;
  final String currentConcern;
  final String lastTurnSummary;
}
