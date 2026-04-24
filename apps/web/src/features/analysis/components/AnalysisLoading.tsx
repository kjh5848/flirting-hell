export function AnalysisLoading() {
  return (
    <div className="rounded-[28px] bg-gray-950 p-5 text-white shadow-soft">
      <p className="text-sm font-bold text-rose-200">분석 중</p>
      <h3 className="mt-2 text-xl font-black">대화 분위기와 말투를 읽는 중입니다.</h3>
      <div className="mt-5 grid gap-2 text-sm text-white/75">
        <span>1. 상대 반응 확인</span>
        <span>2. 내 스타일과 비교</span>
        <span>3. 답장 후보 생성</span>
      </div>
    </div>
  );
}
