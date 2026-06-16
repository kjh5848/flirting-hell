import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/network/api_client.dart';
import '../models/bootstrap_snapshot.dart';

final bootstrapApiProvider = Provider<BootstrapApi>((ref) {
  return BootstrapApi(ref.watch(dioProvider));
});

class BootstrapApi {
  const BootstrapApi(this._dio);

  final Dio _dio;

  Future<BootstrapSnapshot> fetchBootstrap() async {
    final response = await _dio.get<Map<String, dynamic>>('/me/bootstrap');
    final body = response.data;
    if (body == null) {
      throw StateError('Bootstrap response body is empty.');
    }

    final data = body['data'];
    if (data is! Map<String, dynamic>) {
      throw StateError('Bootstrap response data is invalid.');
    }

    return BootstrapSnapshot.fromJson(data);
  }

  Future<BootstrapProfile> updateProfile(UpdateProfilePayload payload) async {
    final response = await _dio.patch<Map<String, dynamic>>(
      '/me/profile',
      data: payload.toJson(),
    );
    final body = response.data;
    if (body == null) {
      throw StateError('Update profile response body is empty.');
    }

    final data = body['data'];
    if (data is! Map<String, dynamic>) {
      throw StateError('Update profile response data is invalid.');
    }

    return BootstrapProfile.fromJson(_asMap(data['profile']));
  }
}

class UpdateProfilePayload {
  const UpdateProfilePayload({
    required this.nickname,
    required this.speechStyle,
    required this.datingStyle,
    required this.guidanceLevel,
    required this.preferredPartnerStyle,
    required this.avoidAdvice,
  });

  final String? nickname;
  final String speechStyle;
  final String datingStyle;
  final String guidanceLevel;
  final String? preferredPartnerStyle;
  final String avoidAdvice;

  Map<String, dynamic> toJson() {
    return {
      'nickname': nickname,
      'speechStyle': speechStyle,
      'datingStyle': datingStyle,
      'guidanceLevel': guidanceLevel,
      'preferredPartnerStyle': preferredPartnerStyle,
      'avoidAdvice': avoidAdvice,
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
