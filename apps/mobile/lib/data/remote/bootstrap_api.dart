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
}
