import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../app/env.dart';

final authControllerProvider = ChangeNotifierProvider<AuthController>((ref) {
  return AuthController(firebaseAuth: _firebaseAuthOrNull());
});

FirebaseAuth? _firebaseAuthOrNull() {
  try {
    return FirebaseAuth.instance;
  } on Object {
    return null;
  }
}

class AuthController extends ChangeNotifier {
  AuthController({FirebaseAuth? firebaseAuth}) : _firebaseAuth = firebaseAuth {
    _authSubscription = _firebaseAuth?.authStateChanges().listen((user) {
      _firebaseUser = user;
      _errorMessage = null;
      notifyListeners();
    });
  }

  final FirebaseAuth? _firebaseAuth;
  StreamSubscription<User?>? _authSubscription;

  bool _isSignedIn = false;
  bool _isSigningIn = false;
  String? _firebaseUid;
  String? _errorMessage;
  User? _firebaseUser;

  bool get isSignedIn => _isSignedIn || _firebaseUser != null;
  bool get isSigningIn => _isSigningIn;
  String? get firebaseUid => _firebaseUid ?? _firebaseUser?.uid;
  String? get errorMessage => _errorMessage;
  bool get canUseFirebaseAuth => _firebaseAuth != null;

  void signInDevelopment() {
    _isSignedIn = true;
    _firebaseUid = 'local-user-1';
    _errorMessage = null;
    notifyListeners();
  }

  Future<void> signInAnonymously() async {
    if (_firebaseAuth == null) {
      _errorMessage = 'Firebase가 아직 초기화되지 않았습니다.';
      notifyListeners();
      return;
    }

    _isSigningIn = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final credential = await _firebaseAuth.signInAnonymously();
      _isSignedIn = false;
      _firebaseUid = null;
      _firebaseUser = credential.user;
    } on FirebaseAuthException catch (error) {
      _errorMessage = error.message ?? error.code;
    } on Object {
      _errorMessage = 'Firebase 로그인 중 오류가 발생했습니다.';
    } finally {
      _isSigningIn = false;
      notifyListeners();
    }
  }

  Future<String?> authorizationToken() async {
    final developmentUid = _firebaseUid;
    if (developmentUid != null) {
      return 'dev:$developmentUid';
    }

    final firebaseUser = _firebaseUser;
    if (firebaseUser == null) {
      return null;
    }

    if (AppEnv.useDevelopmentAuthToken) {
      return 'dev:${firebaseUser.uid}';
    }

    return firebaseUser.getIdToken();
  }

  Future<void> signOut() async {
    _isSignedIn = false;
    _firebaseUid = null;
    _errorMessage = null;
    await _firebaseAuth?.signOut();
    notifyListeners();
  }

  @override
  void dispose() {
    _authSubscription?.cancel();
    super.dispose();
  }
}
