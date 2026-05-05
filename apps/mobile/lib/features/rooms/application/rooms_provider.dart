import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/models/room_models.dart';
import '../../../data/remote/rooms_api.dart';

final roomsProvider = FutureProvider<RoomList>((ref) {
  return ref.watch(roomsApiProvider).fetchRooms();
});

final roomDetailProvider = FutureProvider.family<RoomDetail, String>((ref, roomId) {
  return ref.watch(roomsApiProvider).fetchRoom(roomId);
});
