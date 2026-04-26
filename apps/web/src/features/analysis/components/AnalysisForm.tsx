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
  const messageLines = trimmedText.split("\n").filter(Boolean).length;

  return (
    <Card className="analysis-intake-card space-y-4 border border-white/80 bg-white/95 p-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-black uppercase tracking-[0.18em] text-hell-600">대화 입력</p>
          <h2 className="mt-1 text-2xl font-black tracking-[-0.04em] text-ink">마지막 대화를 붙여넣으세요</h2>
          <p className="mt-1 text-sm leading-6 text-ink-muted">카톡, DM, 문자에서 최근 흐름만 가져오면 됩니다.</p>
        </div>
        <span className="cue-chip">{messageLines}줄</span>
      </div>

      <div className="conversation-guide" aria-label="답장 추천 방식">
        <span>내 말투 반영</span>
        <span>상대 온도 확인</span>
        <span>부담되는 말 제외</span>
      </div>

      <MessageInputCard value={conversationText} onChange={setConversationText} />

      <details className="group rounded-[22px] bg-cream/70 p-3 ring-1 ring-ink/5">
        <summary className="flex cursor-pointer list-none items-center justify-between gap-3">
          <div>
            <p className="text-sm font-black text-ink">분석 조건</p>
            <p className="mt-1 text-xs font-semibold text-ink-muted">{values.relationshipStage} · {values.conversationGoal} · {values.replyIntensity}</p>
          </div>
          <span className="rounded-full bg-white px-3 py-1 text-xs font-black text-ink-muted ring-1 ring-ink/5 group-open:hidden">변경</span>
          <span className="hidden rounded-full bg-white px-3 py-1 text-xs font-black text-ink-muted ring-1 ring-ink/5 group-open:inline">접기</span>
        </summary>
        <div className="mt-4">
          <PreferencePanel value={values} onChange={setValues} />
        </div>
      </details>

      <div className="grid gap-3">
        <div className="rounded-2xl bg-cream p-4 text-sm leading-6 text-ink-muted ring-1 ring-ink/5">
          <strong className="text-ink">개인정보 주의:</strong> 이름, 전화번호, 주소, 회사명처럼 상대를 특정할 수 있는 정보는 삭제하고 분석하세요.
        </div>
        <Button
          type="button"
          className="w-full min-h-12 px-6 text-base"
          disabled={isDisabled}
          onClick={() => onSubmit({ conversationText: trimmedText, ...values, userPreferences: preferences })}
        >
          {isLoading ? "분위기 확인 중..." : "답장 추천받기"}
        </Button>
      </div>

      {trimmedText.length < 20 ? (
        <p className="text-center text-xs font-semibold text-ink-faint">대화를 조금 더 입력하면 분석 버튼이 활성화됩니다.</p>
      ) : null}
    </Card>
  );
}
