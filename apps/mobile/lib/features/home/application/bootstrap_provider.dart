import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/models/bootstrap_snapshot.dart';
import '../../../data/remote/bootstrap_api.dart';
import '../../auth/application/auth_controller.dart';

final bootstrapProvider = FutureProvider<BootstrapSnapshot?>((ref) async {
  final auth = ref.watch(authControllerProvider);

  if (!auth.isSignedIn) {
    return null;
  }

  return ref.watch(bootstrapApiProvider).fetchBootstrap();
});
