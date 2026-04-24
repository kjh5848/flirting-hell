import { useState } from "react";
import type { UserPreferences } from "@flirting-hell/shared";

const storageKey = "flirting-hell:user-preferences";

const defaultPreferences: UserPreferences = {
  datingStyles: ["다정한", "표현 많은"],
  preferredPartnerStyles: ["상냥한", "대화가 잘 통하는"],
  difficultPartnerStyles: ["무뚝뚝한", "표현이 적은"],
  attractionReasons: ["분위기"]
};

function loadPreferences(): UserPreferences {
  try {
    const saved = window.localStorage.getItem(storageKey);
    return saved ? (JSON.parse(saved) as UserPreferences) : defaultPreferences;
  } catch {
    return defaultPreferences;
  }
}

export function useProfileSettings() {
  const [preferences, setPreferences] = useState<UserPreferences>(() => loadPreferences());

  function updatePreferences(next: UserPreferences) {
    setPreferences(next);
    window.localStorage.setItem(storageKey, JSON.stringify(next));
  }

  return {
    preferences,
    updatePreferences
  };
}
