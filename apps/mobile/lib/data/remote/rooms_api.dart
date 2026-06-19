import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/network/api_client.dart';
import '../models/room_models.dart';

final roomsApiProvider = Provider<RoomsApi>((ref) {
  return RoomsApi(ref.watch(dioProvider));
});

class RoomsApi {
  const RoomsApi(this._dio);

  final Dio _dio;

  Future<RoomList> fetchRooms() async {
    final response = await _dio.get<Map<String, dynamic>>('/rooms');
    return RoomList.fromJson(_responseData(response));
  }

  Future<RoomDetail> fetchRoom(String roomId) async {
    final response = await _dio.get<Map<String, dynamic>>('/rooms/$roomId');
    return RoomDetail.fromJson(_responseData(response));
  }

  Future<Room> createRoom(CreateRoomPayload payload) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/rooms',
      data: payload.toJson(),
    );
    final data = _responseData(response);
    final room = data['room'];
    if (room is! Map<String, dynamic>) {
      throw StateError('Create room response data is invalid.');
    }
    return Room.fromJson(room);
  }

  Future<AnalysisTurn> createAnalysis(
    String roomId,
    CreateAnalysisPayload payload,
  ) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/rooms/$roomId/analyses',
      data: payload.toJson(),
    );
    final data = _responseData(response);
    final turn = data['turn'];
    if (turn is! Map<String, dynamic>) {
      throw StateError('Create analysis response data is invalid.');
    }
    return AnalysisTurn.fromJson(turn);
  }

  /// 답장을 원하는 톤 방향으로 다시 받는다(비영속). [direction]은 백엔드 enum 이름.
  Future<String> refineReply(
    String roomId,
    String previousReply,
    String direction,
  ) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/rooms/$roomId/analyses/refine',
      data: {'previousReply': previousReply, 'direction': direction},
    );
    final data = _responseData(response);
    final reply = data['reply'];
    if (reply is! String) {
      throw StateError('Refine reply response data is invalid.');
    }
    return reply;
  }

  /// 분석 답장의 저장(북마크) 상태를 토글한다.
  Future<AnalysisTurn> toggleSaveAnalysis(String roomId, String turnId) async {
    final response = await _dio.patch<Map<String, dynamic>>(
      '/rooms/$roomId/analyses/$turnId/save',
    );
    final data = _responseData(response);
    final turn = data['turn'];
    if (turn is! Map<String, dynamic>) {
      throw StateError('Toggle save response data is invalid.');
    }
    return AnalysisTurn.fromJson(turn);
  }

  /// 저장한 답장 목록(상대별).
  Future<List<SavedReply>> fetchSavedReplies() async {
    final response = await _dio.get<Map<String, dynamic>>('/me/saved-replies');
    final data = _responseData(response);
    final items = data['items'];
    if (items is! List) {
      throw StateError('Saved replies response data is invalid.');
    }
    return items
        .map((item) => SavedReply.fromJson(item as Map<String, dynamic>))
        .toList(growable: false);
  }

  /// 데이트 플랜을 받는다(비영속). 코스 + 궁합 확인 포인트.
  Future<DatePlan> fetchPlan(String roomId) async {
    final response = await _dio.get<Map<String, dynamic>>('/rooms/$roomId/plan');
    final data = _responseData(response);
    final plan = data['plan'];
    if (plan is! Map<String, dynamic>) {
      throw StateError('Plan response data is invalid.');
    }
    return DatePlan.fromJson(plan);
  }

  /// 코치와의 멀티턴 대화에 한 번 응답받는다(상태 비저장). [history]는
  /// `{role: 'USER'|'COACH', text: ...}` 목록(이번 메시지 이전까지).
  Future<String> coachReply(
    String roomId,
    List<Map<String, String>> history,
    String userMessage,
  ) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/rooms/$roomId/coach',
      data: {'history': history, 'userMessage': userMessage},
    );
    final data = _responseData(response);
    final reply = data['reply'];
    if (reply is! String) {
      throw StateError('Coach reply response data is invalid.');
    }
    return reply;
  }

  Map<String, dynamic> _responseData(Response<Map<String, dynamic>> response) {
    final body = response.data;
    if (body == null) {
      throw StateError('Response body is empty.');
    }

    final data = body['data'];
    if (data is! Map<String, dynamic>) {
      throw StateError('Response data is invalid.');
    }

    return data;
  }
}
