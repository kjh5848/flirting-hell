import { useState } from "react";
import type { AnalysisRequest, CreateAnalysisData } from "@flirting-hell/shared";
import { ApiClientError } from "../../../shared/lib/apiClient";
import { createAnalysis } from "../api/analysisApi";

type UseAnalysisOptions = {
  onCompleted?: () => void;
};

export function useAnalysis(options: UseAnalysisOptions = {}) {
  const [data, setData] = useState<CreateAnalysisData | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  async function analyze(input: AnalysisRequest) {
    setIsLoading(true);
    setError(null);

    try {
      const nextData = await createAnalysis(input);
      setData(nextData);
      options.onCompleted?.();
    } catch (caught) {
      if (caught instanceof ApiClientError || caught instanceof Error) {
        setError(caught.message);
      } else {
        setError("분석 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
      }
    } finally {
      setIsLoading(false);
    }
  }

  function reset() {
    setData(null);
    setError(null);
    setIsLoading(false);
  }

  return {
    data,
    error,
    isLoading,
    analyze,
    reset
  };
}
