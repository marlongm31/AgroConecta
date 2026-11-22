import { useCallback, useEffect, useRef, useState } from 'react';

export type VoiceRecorderState = 'idle' | 'requesting' | 'recording';

export interface VoiceRecordingResult {
  file: File;
  durationSeconds: number;
}

function preferredMimeType(): string | undefined {
  if (typeof MediaRecorder === 'undefined' || typeof MediaRecorder.isTypeSupported !== 'function') {
    return undefined;
  }
  return ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4', 'audio/ogg']
    .find((type) => MediaRecorder.isTypeSupported(type));
}

function extensionFor(mimeType: string): string {
  if (mimeType.includes('mp4')) return 'm4a';
  if (mimeType.includes('ogg')) return 'ogg';
  if (mimeType.includes('wav')) return 'wav';
  return 'webm';
}

function normalizedMimeType(mimeType: string): string {
  const base = mimeType.split(';')[0].trim();
  return base || 'audio/webm';
}

export function useVoiceRecorder() {
  const [state, setState] = useState<VoiceRecorderState>('idle');
  const [elapsed, setElapsed] = useState(0);
  const recorderRef = useRef<MediaRecorder | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const chunksRef = useRef<Blob[]>([]);
  const startedAtRef = useRef(0);
  const resolveRef = useRef<((result: VoiceRecordingResult | null) => void) | null>(null);

  const reset = useCallback(() => {
    streamRef.current?.getTracks().forEach((track) => track.stop());
    recorderRef.current = null;
    streamRef.current = null;
    chunksRef.current = [];
    startedAtRef.current = 0;
    setState('idle');
    setElapsed(0);
  }, []);

  const settle = useCallback((result: VoiceRecordingResult | null) => {
    const resolve = resolveRef.current;
    resolveRef.current = null;
    resolve?.(result);
  }, []);

  const start = useCallback(async (): Promise<VoiceRecordingResult | null> => {
    if (state !== 'idle') return null;
    if (typeof navigator === 'undefined' || !navigator.mediaDevices?.getUserMedia
        || typeof MediaRecorder === 'undefined') {
      throw new Error('Este navegador no permite grabar audio. Usa un dispositivo con micrófono.');
    }
    setState('requesting');
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mimeType = preferredMimeType();
      const recorder = new MediaRecorder(stream, mimeType ? { mimeType } : undefined);
      chunksRef.current = [];
      recorder.ondataavailable = (event) => {
        if (event.data.size > 0) chunksRef.current.push(event.data);
      };
      recorder.onerror = () => {
        settle(null);
        reset();
      };
      recorder.start();
      recorderRef.current = recorder;
      streamRef.current = stream;
      startedAtRef.current = Date.now();
      setState('recording');
      return await new Promise<VoiceRecordingResult | null>((resolve) => {
        resolveRef.current = resolve;
      });
    } catch (caught) {
      reset();
      if (caught instanceof Error && caught.message.includes('permiso')) throw caught;
      throw new Error('No se pudo acceder al micrófono. Revisa los permisos del navegador.');
    }
  }, [reset, settle, state]);

  const stop = useCallback((cancel = false) => {
    const recorder = recorderRef.current;
    if (!recorder || recorder.state === 'inactive') {
      settle(null);
      reset();
      return;
    }
    if (cancel) {
      recorder.onstop = null;
      recorder.onerror = null;
      recorder.stop();
      settle(null);
      reset();
      return;
    }
    recorder.onstop = () => {
      const recordedType = normalizedMimeType(recorder.mimeType || 'audio/webm');
      const blob = new Blob(chunksRef.current, { type: recordedType });
      const extension = extensionFor(recordedType);
      const file = new File([blob], `nota-de-voz-${Date.now()}.${extension}`, { type: recordedType });
      const durationSeconds = Math.max(1, Math.round((Date.now() - startedAtRef.current) / 1000));
      settle({ file, durationSeconds });
      reset();
    };
    recorder.stop();
  }, [reset, settle]);

  useEffect(() => {
    if (state !== 'recording') return;
    const timer = window.setInterval(() => {
      setElapsed(Math.max(0, Math.floor((Date.now() - startedAtRef.current) / 1000)));
    }, 250);
    return () => window.clearInterval(timer);
  }, [state]);

  useEffect(() => () => {
    const recorder = recorderRef.current;
    if (recorder && recorder.state !== 'inactive') {
      recorder.onstop = null;
      recorder.onerror = null;
      recorder.stop();
    }
    streamRef.current?.getTracks().forEach((track) => track.stop());
  }, []);

  return {
    state,
    elapsed,
    start,
    stop: () => stop(false),
    cancel: () => stop(true),
  };
}
