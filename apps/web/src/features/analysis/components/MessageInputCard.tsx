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
          <p className="text-sm font-bold text-gray-950">대화 내용</p>
          <p className="mt-1 text-xs leading-5 text-gray-500">`나:`와 `상대:` 형식이면 말투 분석이 더 안정적입니다.</p>
        </div>
        <span className="shrink-0 rounded-full bg-gray-100 px-3 py-1 text-xs font-bold text-gray-500">{value.length}자</span>
      </div>
      <Textarea
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={`나: 오늘 뭐해?\n상대: 그냥 집에 있어 ㅋㅋ\n나: 오 쉬는 날이네`}
      />
    </div>
  );
}
