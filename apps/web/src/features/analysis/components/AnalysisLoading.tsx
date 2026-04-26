export function AnalysisLoading() {
  return (
    <div className="loading-transition rounded-[30px] bg-white p-5 text-ink shadow-soft ring-1 ring-black/[0.04]">
      <div className="message-transition" aria-hidden="true">
        <span className="message-bubble mine">나: 오늘 뭐해?</span>
        <span className="message-bubble theirs">상대: 그냥 집에 있어 ㅋㅋ</span>
        <span className="reply-preview">답장 준비 중</span>
      </div>
      <p className="text-sm font-black text-hell-600">분위기 확인 중</p>
      <h3 className="mt-2 text-2xl font-black tracking-[-0.04em]">대화의 온도를 보고 있어요.</h3>
      <div className="human-progress-list mt-5 grid gap-2 text-sm text-ink-muted">
        <span className="human-progress-step is-live">상대가 편하게 답할 수 있는지 확인</span>
        <span className="human-progress-step">내 말투와 어색하지 않은지 맞춤</span>
        <span className="human-progress-step">부담스럽거나 재촉하는 표현 제외</span>
      </div>
    </div>
  );
}
