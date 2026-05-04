enum AppEnvironment {
  local,
  staging,
  production,
}

class AppEnv {
  const AppEnv._();

  static const current = AppEnvironment.local;
  static const apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://localhost:8080/api',
  );
}
