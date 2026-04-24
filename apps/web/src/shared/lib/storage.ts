const anonymousUserIdKey = "flirting-hell:anonymous-user-id";

export function getAnonymousUserId(): string {
  const current = window.localStorage.getItem(anonymousUserIdKey);
  if (current) {
    return current;
  }

  const next = crypto.randomUUID();
  window.localStorage.setItem(anonymousUserIdKey, next);
  return next;
}
