"use client";

import { useCallback, useEffect, useRef, useState, useTransition } from "react";

interface UsePollingOptions<T> {
  /** The async function to fetch data */
  fetcher: () => Promise<T>;
  /** Polling interval in milliseconds (default: 3000) */
  intervalMs?: number;
  /** Whether polling is enabled (default: true) */
  enabled?: boolean;
  /** Function to determine if polling should stop based on data */
  shouldStop?: (data: T) => boolean;
  /** Callback when data is fetched */
  onData?: (data: T) => void;
  /** Callback when an error occurs */
  onError?: (error: Error) => void;
}

interface UsePollingResult<T> {
  /** The latest fetched data */
  data: T | null;
  /** Whether a fetch is currently in progress */
  isLoading: boolean;
  /** Whether polling is actively running */
  isPolling: boolean;
  /** Any error that occurred during fetching */
  error: Error | null;
  /** Manually trigger a refresh */
  refresh: () => void;
  /** Start polling */
  start: () => void;
  /** Stop polling */
  stop: () => void;
}

/**
 * Hook for polling data at regular intervals.
 * Automatically pauses when the tab is not visible.
 * Integrates with React's useTransition for non-blocking updates.
 */
export function usePolling<T>({
  fetcher,
  intervalMs = 3000,
  enabled = true,
  shouldStop,
  onData,
  onError,
}: UsePollingOptions<T>): UsePollingResult<T> {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [isPolling, setIsPolling] = useState(false);
  const [isPending, startTransition] = useTransition();

  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const isVisibleRef = useRef(true);
  const isMountedRef = useRef(true);
  
  // Store fetcher and callbacks in refs to avoid dependency issues
  const fetcherRef = useRef(fetcher);
  const shouldStopRef = useRef(shouldStop);
  const onDataRef = useRef(onData);
  const onErrorRef = useRef(onError);
  
  // Update refs when props change
  useEffect(() => {
    fetcherRef.current = fetcher;
    shouldStopRef.current = shouldStop;
    onDataRef.current = onData;
    onErrorRef.current = onError;
  });

  const fetchData = useCallback(async () => {
    if (!isMountedRef.current || !isVisibleRef.current) return;

    try {
      const result = await fetcherRef.current();

      if (!isMountedRef.current) return;

      startTransition(() => {
        setData(result);
        setError(null);
        onDataRef.current?.(result);

        // Check if we should stop polling
        if (shouldStopRef.current?.(result)) {
          setIsPolling(false);
          if (intervalRef.current) {
            clearInterval(intervalRef.current);
            intervalRef.current = null;
          }
        }
      });
    } catch (err) {
      if (!isMountedRef.current) return;

      const error = err instanceof Error ? err : new Error(String(err));
      setError(error);
      onErrorRef.current?.(error);
    }
  }, []);

  const stopPolling = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    setIsPolling(false);
  }, []);

  const startPolling = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }

    setIsPolling(true);
    fetchData(); // Fetch immediately

    intervalRef.current = setInterval(() => {
      if (isVisibleRef.current) {
        fetchData();
      }
    }, intervalMs);
  }, [fetchData, intervalMs]);

  const refresh = useCallback(() => {
    fetchData();
  }, [fetchData]);

  // Handle visibility change
  useEffect(() => {
    const handleVisibilityChange = () => {
      isVisibleRef.current = document.visibilityState === "visible";

      // If becoming visible and polling is enabled, fetch immediately
      if (isVisibleRef.current && isPolling) {
        fetchData();
      }
    };

    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [isPolling, fetchData]);

  // Start/stop polling based on enabled prop - only run once on mount or when enabled changes
  useEffect(() => {
    if (enabled) {
      startPolling();
    } else {
      stopPolling();
    }

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [enabled, intervalMs]); // eslint-disable-line react-hooks/exhaustive-deps

  // Cleanup on unmount
  useEffect(() => {
    isMountedRef.current = true;

    return () => {
      isMountedRef.current = false;
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);

  return {
    data,
    isLoading: isPending,
    isPolling,
    error,
    refresh,
    start: startPolling,
    stop: stopPolling,
  };
}

/**
 * Hook for polling workflow status.
 * Automatically stops when workflow reaches a terminal state.
 */
export function useWorkflowPolling(
  fetchStatus: () => Promise<{ status: string; currentStep?: string; blockingReason?: string | null }>,
  options?: Omit<UsePollingOptions<{ status: string; currentStep?: string; blockingReason?: string | null }>, "fetcher" | "shouldStop">
) {
  const terminalStates = ["COMPLETED", "CANCELLED", "FAILED"];

  return usePolling({
    ...options,
    fetcher: fetchStatus,
    shouldStop: (data) => terminalStates.includes(data.status),
    intervalMs: options?.intervalMs ?? 2000,
  });
}

