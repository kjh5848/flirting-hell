import {
  conversationGoals,
  guidanceModes,
  relationshipStages,
  replyIntensities,
  toneModes,
  type AnalysisRequest
} from "@flirting-hell/shared";
import { Chip } from "../../../shared/ui/Chip";

type PreferencePanelProps = {
  value: Pick<AnalysisRequest, "relationshipStage" | "conversationGoal" | "replyIntensity" | "guidanceMode" | "toneMode">;
  onChange: (value: PreferencePanelProps["value"]) => void;
};

const groups = [
  { key: "relationshipStage", title: "관계 단계", options: relationshipStages },
  { key: "conversationGoal", title: "대화 목적", options: conversationGoals },
  { key: "replyIntensity", title: "답장 강도", options: replyIntensities },
  { key: "guidanceMode", title: "조언 수위", options: guidanceModes },
  { key: "toneMode", title: "말투 반영", options: toneModes }
] as const;

export function PreferencePanel({ value, onChange }: PreferencePanelProps) {
  return (
    <div className="space-y-4">
      {groups.map((group) => (
        <div key={group.key}>
          <p className="mb-2 text-sm font-black text-ink">{group.title}</p>
          <div className="flex flex-wrap gap-1.5">
            {group.options.map((option) => (
              <Chip key={option} selected={value[group.key] === option} onClick={() => onChange({ ...value, [group.key]: option })}>
                {option}
              </Chip>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
