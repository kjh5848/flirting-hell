import {
  attractionReasonOptions,
  datingStyleOptions,
  difficultPartnerStyleOptions,
  preferredPartnerStyleOptions,
  type UserPreferences
} from "@flirting-hell/shared";
import { Chip } from "../../../shared/ui/Chip";

type StylePreferenceFormProps = {
  value: UserPreferences;
  onChange: (value: UserPreferences) => void;
};

type PreferenceKey = keyof UserPreferences;

function toggle(values: string[], option: string): string[] {
  return values.includes(option) ? values.filter((value) => value !== option) : [...values, option];
}

const groups: Array<{ key: PreferenceKey; title: string; options: readonly string[] }> = [
  { key: "datingStyles", title: "원하는 연애 스타일", options: datingStyleOptions },
  { key: "preferredPartnerStyles", title: "선호하는 상대 스타일", options: preferredPartnerStyleOptions },
  { key: "difficultPartnerStyles", title: "어려워하는 상대 스타일", options: difficultPartnerStyleOptions },
  { key: "attractionReasons", title: "지금 끌리는 이유", options: attractionReasonOptions }
];

export function StylePreferenceForm({ value, onChange }: StylePreferenceFormProps) {
  return (
    <div className="space-y-5">
      {groups.map((group) => (
        <div key={group.key}>
          <p className="mb-2 text-sm font-bold text-gray-800">{group.title}</p>
          <div className="flex flex-wrap gap-2">
            {group.options.map((option) => (
              <Chip
                key={option}
                selected={value[group.key].includes(option)}
                onClick={() => onChange({ ...value, [group.key]: toggle(value[group.key], option) })}
              >
                {option}
              </Chip>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
