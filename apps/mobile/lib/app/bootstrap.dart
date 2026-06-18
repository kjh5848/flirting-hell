import 'package:firebase_core/firebase_core.dart';

import '../firebase_options.dart';
import 'env.dart';

class AppBootstrap {
  const AppBootstrap._();

  static Future<void> initialize() async {
    // dev 인증 모드에서는 Firebase 설정 없이도 앱을 띄울 수 있어야 한다.
    // (mock 시연·로컬 개발: dev 토큰으로 백엔드와 통신, Firebase 미설정 허용)
    if (AppEnv.useDevelopmentAuthToken) {
      return;
    }
    await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform,
    );
  }
}
