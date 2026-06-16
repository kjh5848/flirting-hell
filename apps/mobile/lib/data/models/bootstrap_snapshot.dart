class BootstrapSnapshot {
  const BootstrapSnapshot({
    required this.user,
    required this.usage,
    required this.recentRooms,
  });

  factory BootstrapSnapshot.fromJson(Map<String, dynamic> json) {
    return BootstrapSnapshot(
      user: BootstrapUser.fromJson(_asMap(json['user'])),
      usage: BootstrapUsage.fromJson(_asMap(json['usage'])),
      recentRooms: _asList(json['recentRooms'])
          .map((item) => BootstrapRecentRoom.fromJson(_asMap(item)))
          .toList(growable: false),
    );
  }

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

  factory BootstrapUser.fromJson(Map<String, dynamic> json) {
    return BootstrapUser(
      userId: json['userId'] as String,
      onboardingCompleted: json['onboardingCompleted'] as bool,
      profile: BootstrapProfile.fromJson(_asMap(json['profile'])),
    );
  }

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
    this.personalitySelf,
    this.personalityIdeal,
  });

  factory BootstrapProfile.fromJson(Map<String, dynamic> json) {
    return BootstrapProfile(
      nickname: json['nickname'] as String?,
      speechStyle: json['speechStyle'] as String,
      datingStyle: json['datingStyle'] as String,
      guidanceLevel: json['guidanceLevel'] as String,
      preferredPartnerStyle: json['preferredPartnerStyle'] as String?,
      avoidAdvice: json['avoidAdvice'] as String,
      personalitySelf: json['personalitySelf'] as String?,
      personalityIdeal: json['personalityIdeal'] as String?,
    );
  }

  final String? nickname;
  final String speechStyle;
  final String datingStyle;
  final String guidanceLevel;
  final String? preferredPartnerStyle;
  final String avoidAdvice;

  /// 연애 성향 5축 JSON 문자열(서버는 해석 안 함). nullable.
  final String? personalitySelf;
  final String? personalityIdeal;
}

class BootstrapUsage {
  const BootstrapUsage({
    required this.creditBalance,
    required this.freeRemaining,
    required this.rewardAdRemaining,
  });

  factory BootstrapUsage.fromJson(Map<String, dynamic> json) {
    return BootstrapUsage(
      creditBalance: json['creditBalance'] as int,
      freeRemaining: _asMap(json['freeAnalyses'])['remaining'] as int,
      rewardAdRemaining: _asMap(json['rewardAds'])['remaining'] as int,
    );
  }

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

  factory BootstrapRecentRoom.fromJson(Map<String, dynamic> json) {
    return BootstrapRecentRoom(
      roomId: json['roomId'] as String,
      alias: json['alias'] as String,
      currentConcern: json['currentConcern'] as String,
      lastTurnSummary: json['lastTurnSummary'] as String,
    );
  }

  final String roomId;
  final String alias;
  final String currentConcern;
  final String lastTurnSummary;
}

Map<String, dynamic> _asMap(Object? value) {
  if (value is Map<String, dynamic>) {
    return value;
  }
  if (value is Map) {
    return value.cast<String, dynamic>();
  }
  throw StateError('Expected JSON object, got $value.');
}

List<Object?> _asList(Object? value) {
  if (value is List) {
    return value;
  }
  throw StateError('Expected JSON list, got $value.');
}
