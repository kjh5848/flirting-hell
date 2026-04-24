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
          <p className="mt-1 text-xs leading-5 text-gray-500">최근 대화 10~30줄을 권장합니다. 이름, 전화번호, 주소는 지우고 넣으세요.</p>
        </div>
        <span className="shrink-0 text-xs font-semibold text-gray-400">{value.length}자</span>
      </div>
      <Textarea
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={`나: 오늘 뭐해?
상대: 그냥 집에 있어 ㅋㅋ
나: 오 쉬는 날이네`}
      />
    </div>
  );
}
