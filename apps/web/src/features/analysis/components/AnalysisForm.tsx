import { useState } from "react";
import type { AnalysisRequest, UserPreferences } from "@flirting-hell/shared";
import { Button } from "../../../shared/ui/Button";
import { Card } from "../../../shared/ui/Card";
import { MessageInputCard } from "./MessageInputCard";
import { PreferencePanel } from "./PreferencePanel";

type AnalysisFormProps = {
  preferences: UserPreferences;
  isLoading: boolean;
  onSubmit: (input: AnalysisRequest) => void;
};

const defaultPreferenceValues: Pick<AnalysisRequest, "relationshipStage" | "conversationGoal" | "replyIntensity" | "guidanceMode" | "toneMode"> = {
  relationshipStage: "썸",
  conversationGoal: "대화 이어가기",
  replyIntensity: "설렘맛",
  guidanceMode: "균형 조언",
  toneMode: "자동 분석"
};

export function AnalysisForm({ preferences, isLoading, onSubmit }: AnalysisFormProps) {
  const [conversationText, setConversationText] = useState(`나: 오늘 뭐해?\n상대: 그냥 집에 있어 ㅋㅋ\n나: 오 쉬는 날이네. 뭐하면서 쉬고 있어?`);
  const [values, setValues] = useState(defaultPreferenceValues);

  const isDisabled = isLoading || conversationText.trim().length < 20;

  return (
    <Card className="space-y-6">
      <MessageInputCard value={conversationText} onChange={setConversationText} />
      <PreferencePanel value={values} onChange={setValues} />
      <div className="rounded-2xl bg-gray-50 p-4 text-sm leading-6 text-gray-600">
        <strong className="text-gray-950">개인정보 주의:</strong> 이름, 전화번호, 주소, 회사명처럼 상대를 특정할 수 있는 정보는 삭제하고 분석하세요.
      </div>
      <Button
        type="button"
        className="w-full text-base"
        disabled={isDisabled}
        onClick={() => onSubmit({ conversationText, ...values, userPreferences: preferences })}
      >
        {isLoading ? "분석 중..." : "AI로 답장 찾기"}
      </Button>
    </Card>
  );
}
