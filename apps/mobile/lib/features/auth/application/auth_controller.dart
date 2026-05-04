import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final authControllerProvider = ChangeNotifierProvider<AuthController>((ref) {
  return AuthController();
});

class AuthController extends ChangeNotifier {
  bool _isSignedIn = false;
  String? _firebaseUid;

  bool get isSignedIn => _isSignedIn;
  String? get firebaseUid => _firebaseUid;

  void signInDevelopment() {
    _isSignedIn = true;
    _firebaseUid = 'local-user-1';
    notifyListeners();
  }

  void signOut() {
    _isSignedIn = false;
    _firebaseUid = null;
    notifyListeners();
  }
}
