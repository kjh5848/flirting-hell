import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../core/widgets/app_shell.dart';
import '../features/auth/application/auth_controller.dart';
import '../features/auth/presentation/auth_screen.dart';
import '../features/auth/presentation/splash_screen.dart';
import '../features/billing/presentation/billing_screen.dart';
import '../features/home/presentation/home_screen.dart';
import '../features/onboarding/presentation/onboarding_screen.dart';
import '../features/personality/presentation/personality_setup_screen.dart';
import '../features/profile/presentation/profile_screen.dart';
import '../features/rooms/presentation/room_detail_screen.dart';
import '../features/rooms/presentation/rooms_screen.dart';
import '../features/saved_replies/presentation/saved_replies_screen.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final authController = ref.watch(authControllerProvider);

  return GoRouter(
    initialLocation: '/splash',
    refreshListenable: authController,
    redirect: (context, state) {
      final isSignedIn = authController.isSignedIn;
      final path = state.uri.path;
      final isPublicPath = path == '/splash' || path == '/auth';

      if (!isSignedIn && !isPublicPath) {
        return '/auth';
      }

      if (isSignedIn && isPublicPath) {
        return '/onboarding';
      }

      return null;
    },
    routes: [
      GoRoute(
        path: '/splash',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: '/auth',
        builder: (context, state) => const AuthScreen(),
      ),
      GoRoute(
        path: '/onboarding',
        builder: (context, state) => const OnboardingScreen(),
      ),
      ShellRoute(
        builder: (context, state, child) {
          return AppShell(
            currentPath: state.uri.path,
            child: child,
          );
        },
        routes: [
          GoRoute(
            path: '/home',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: HomeScreen(),
            ),
          ),
          GoRoute(
            path: '/rooms',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: RoomsScreen(),
            ),
          ),
          GoRoute(
            path: '/rooms/:roomId',
            pageBuilder: (context, state) => NoTransitionPage(
              child: RoomDetailScreen(
                roomId: state.pathParameters['roomId']!,
              ),
            ),
          ),
          GoRoute(
            path: '/saved',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: SavedRepliesScreen(),
            ),
          ),
          GoRoute(
            path: '/profile',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: ProfileScreen(),
            ),
          ),
          GoRoute(
            path: '/billing',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: BillingScreen(),
            ),
          ),
          GoRoute(
            path: '/personality',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: PersonalitySetupScreen(),
            ),
          ),
        ],
      ),
    ],
  );
});
