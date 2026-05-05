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
