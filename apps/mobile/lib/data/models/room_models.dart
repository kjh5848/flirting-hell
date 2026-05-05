class RoomList {
  const RoomList({
    required this.rooms,
    required this.nextCursor,
  });

  factory RoomList.fromJson(Map<String, dynamic> json) {
    return RoomList(
      rooms: _asList(json['rooms'])
          .map((item) => RoomSummary.fromJson(_asMap(item)))
          .toList(growable: false),
      nextCursor: json['nextCursor'] as String?,
    );
  }

  final List<RoomSummary> rooms;
  final String? nextCursor;
}

class RoomSummary {
  const RoomSummary({
    required this.roomId,
    required this.alias,
    required this.relationshipStage,
    required this.currentConcern,
    required this.lastTurnSummary,
    required this.lastActivityAt,
    required this.savedReplyCount,
  });

  factory RoomSummary.fromJson(Map<String, dynamic> json) {
    return RoomSummary(
      roomId: json['roomId'] as String,
      alias: json['alias'] as String,
      relationshipStage: json['relationshipStage'] as String,
      currentConcern: json['currentConcern'] as String?,
      lastTurnSummary: json['lastTurnSummary'] as String,
      lastActivityAt: DateTime.parse(json['lastActivityAt'] as String),
      savedReplyCount: json['savedReplyCount'] as int,
    );
  }

  final String roomId;
  final String alias;
  final String relationshipStage;
  final String? currentConcern;
  final String lastTurnSummary;
  final DateTime lastActivityAt;
  final int savedReplyCount;
}

class RoomDetail {
  const RoomDetail({
    required this.room,
    required this.recentTurns,
    required this.savedReplies,
  });

  factory RoomDetail.fromJson(Map<String, dynamic> json) {
    return RoomDetail(
      room: Room.fromJson(_asMap(json['room'])),
      recentTurns: _asList(json['recentTurns'])
          .map((item) => AnalysisTurn.fromJson(_asMap(item)))
          .toList(growable: false),
      savedReplies: _asList(json['savedReplies']),
    );
  }

  final Room room;
  final List<AnalysisTurn> recentTurns;
  final List<Object?> savedReplies;
}

class AnalysisTurn {
  const AnalysisTurn({
    required this.turnId,
    required this.sourceType,
    required this.participantSummary,
    required this.summary,
    required this.currentState,
    required this.recommendedStrategyId,
    required this.warnings,
    required this.primaryReply,
    required this.alternativeReplies,
    required this.replyReason,
    required this.nextAction,
    required this.createdAt,
  });

  factory AnalysisTurn.fromJson(Map<String, dynamic> json) {
    return AnalysisTurn(
      turnId: json['turnId'] as String,
      sourceType: json['sourceType'] as String,
      participantSummary: json['participantSummary'] as String,
      summary: json['summary'] as String,
      currentState: json['currentState'] as String,
      recommendedStrategyId: json['recommendedStrategyId'] as String,
      warnings: _asStringList(json['warnings']),
      primaryReply: json['primaryReply'] as String,
      alternativeReplies: _asStringList(json['alternativeReplies']),
      replyReason: json['replyReason'] as String,
      nextAction: json['nextAction'] as String,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }

  final String turnId;
  final String sourceType;
  final String participantSummary;
  final String summary;
  final String currentState;
  final String recommendedStrategyId;
  final List<String> warnings;
  final String primaryReply;
  final List<String> alternativeReplies;
  final String replyReason;
  final String nextAction;
  final DateTime createdAt;
}

class Room {
  const Room({
    required this.roomId,
    required this.alias,
    required this.relationshipStage,
    required this.currentConcern,
    required this.cautionNotes,
    required this.preferredStrategyId,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Room.fromJson(Map<String, dynamic> json) {
    return Room(
      roomId: json['roomId'] as String,
      alias: json['alias'] as String,
      relationshipStage: json['relationshipStage'] as String,
      currentConcern: json['currentConcern'] as String?,
      cautionNotes: json['cautionNotes'] as String?,
      preferredStrategyId: json['preferredStrategyId'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
      updatedAt: DateTime.parse(json['updatedAt'] as String),
    );
  }

  final String roomId;
  final String alias;
  final String relationshipStage;
  final String? currentConcern;
  final String? cautionNotes;
  final String? preferredStrategyId;
  final DateTime createdAt;
  final DateTime updatedAt;
}

class CreateRoomPayload {
  const CreateRoomPayload({
    required this.alias,
    required this.relationshipStage,
    required this.currentConcern,
    required this.cautionNotes,
    required this.preferredStrategyId,
  });

  final String alias;
  final String relationshipStage;
  final String? currentConcern;
  final String? cautionNotes;
  final String? preferredStrategyId;

  Map<String, dynamic> toJson() {
    return {
      'alias': alias,
      'relationshipStage': relationshipStage,
      'currentConcern': currentConcern,
      'cautionNotes': cautionNotes,
      'preferredStrategyId': preferredStrategyId,
    };
  }
}

class CreateAnalysisPayload {
  const CreateAnalysisPayload({
    required this.rawInput,
    required this.requestedStrategyId,
  });

  final String rawInput;
  final String? requestedStrategyId;

  Map<String, dynamic> toJson() {
    return {
      'rawInput': rawInput,
      'requestedStrategyId': requestedStrategyId,
    };
  }
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

List<String> _asStringList(Object? value) {
  return _asList(value).map((item) => item as String).toList(growable: false);
}
