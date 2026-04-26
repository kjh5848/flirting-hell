import { Textarea } from "../../../shared/ui/Textarea";

type MessageInputCardProps = {
  value: string;
  onChange: (value: string) => void;
};

export function MessageInputCard({ value, onChange }: MessageInputCardProps) {
  return (
    <div>
      <div className="mb-3 flex items-end justify-between gap-3">
        <div>
          <p className="text-sm font-black text-ink">대화 내용</p>
          <p className="mt-1 text-xs leading-5 text-ink-muted">`나:`와 `상대:` 형식이면 더 정확합니다.</p>
        </div>
        <span className="shrink-0 rounded-full bg-cream px-3 py-1 text-xs font-bold text-ink-muted ring-1 ring-ink/5">{value.length}자</span>
      </div>
      <Textarea
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={`나: 오늘 뭐해?\n상대: 그냥 집에 있어 ㅋㅋ\n나: 오 쉬는 날이네`}
      />
    </div>
  );
}
