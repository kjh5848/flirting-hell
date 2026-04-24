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

  const trimmedText = conversationText.trim();
  const isDisabled = isLoading || trimmedText.length < 20;

  return (
    <Card className="space-y-6">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400">Analysis Input</p>
          <h2 className="mt-2 text-2xl font-black tracking-[-0.03em] text-gray-950">대화 붙여넣기</h2>
          <p className="mt-2 text-sm leading-6 text-gray-500">최근 흐름을 알 수 있게 10~30줄 정도가 가장 좋습니다.</p>
        </div>
        <span className="rounded-full bg-rose-50 px-3 py-1 text-xs font-bold text-hell-600 ring-1 ring-rose-100">Mock UI</span>
      </div>

      <MessageInputCard value={conversationText} onChange={setConversationText} />
      <PreferencePanel value={values} onChange={setValues} />

      <div className="grid gap-3 sm:grid-cols-[1fr_auto] sm:items-center">
        <div className="rounded-2xl bg-gray-50 p-4 text-sm leading-6 text-gray-600 ring-1 ring-gray-100">
          <strong className="text-gray-950">개인정보 주의:</strong> 이름, 전화번호, 주소, 회사명처럼 상대를 특정할 수 있는 정보는 삭제하고 분석하세요.
        </div>
        <Button
          type="button"
          className="w-full px-6 text-base sm:w-auto"
          disabled={isDisabled}
          onClick={() => onSubmit({ conversationText: trimmedText, ...values, userPreferences: preferences })}
        >
          {isLoading ? "분석 중..." : "AI로 답장 찾기"}
        </Button>
      </div>

      {trimmedText.length < 20 ? (
        <p className="text-center text-xs font-semibold text-gray-400">대화를 조금 더 입력하면 분석 버튼이 활성화됩니다.</p>
      ) : null}
    </Card>
  );
}
