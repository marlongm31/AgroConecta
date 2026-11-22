import { describe, expect, it } from 'vitest';
import { formatAttachmentDuration, validateChatFile } from './chatAttachments';

function fileFrom(bytes: number[], name: string, type: string): File {
  return new File([new Uint8Array(bytes)], name, { type });
}

const mp4 = [0, 0, 0, 0x18, 0x66, 0x74, 0x79, 0x70, 0x69, 0x73, 0x6F, 0x6D];
const webm = [0x1A, 0x45, 0xDF, 0xA3, 0x77, 0x65, 0x62, 0x6D, 0, 0, 0, 0];
const wav = [0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x41, 0x56, 0x45];

describe('validateChatFile', () => {
  it('acepta videos MP4 y WebM dentro del límite de 5 MB', async () => {
    const mp4Result = await validateChatFile(fileFrom(mp4, 'cosecha.mp4', 'video/mp4'));
    expect(mp4Result.ok).toBe(true);
    if (mp4Result.ok) expect(mp4Result.value.contentType).toBe('video/mp4');

    const webmResult = await validateChatFile(fileFrom(webm, 'cosecha.webm', 'video/webm'));
    expect(webmResult.ok).toBe(true);
    if (webmResult.ok) expect(webmResult.value.contentType).toBe('video/webm');
  });

  it('acepta notas de voz WebM, WAV, OGG, MP3 y M4A', async () => {
    const webmResult = await validateChatFile(fileFrom(webm, 'nota.webm', 'audio/webm'));
    expect(webmResult.ok).toBe(true);
    if (webmResult.ok) expect(webmResult.value.contentType).toBe('audio/webm');

    const wavResult = await validateChatFile(fileFrom(wav, 'nota.wav', 'audio/wav'));
    expect(wavResult.ok).toBe(true);
    if (wavResult.ok) expect(wavResult.value.contentType).toBe('audio/wav');

    const oggResult = await validateChatFile(fileFrom([0x4F, 0x67, 0x67, 0x53], 'nota.ogg', 'audio/ogg'));
    expect(oggResult.ok).toBe(true);
    if (oggResult.ok) expect(oggResult.value.contentType).toBe('audio/ogg');

    const mp3Result = await validateChatFile(fileFrom([0x49, 0x44, 0x33], 'nota.mp3', 'audio/mpeg'));
    expect(mp3Result.ok).toBe(true);
    if (mp3Result.ok) expect(mp3Result.value.contentType).toBe('audio/mpeg');

    const m4aResult = await validateChatFile(fileFrom(mp4, 'nota.m4a', 'audio/mp4'));
    expect(m4aResult.ok).toBe(true);
    if (m4aResult.ok) expect(m4aResult.value.contentType).toBe('audio/mp4');
  });

  it('rechaza extensión que no coincide con el contenido multimedia', async () => {
    const result = await validateChatFile(fileFrom(mp4, 'cosecha.webm', 'video/webm'));
    expect(result).toEqual({ ok: false, message: 'La extensión del archivo no coincide con su contenido.' });
  });

  it('rechaza archivos vacíos', async () => {
    const result = await validateChatFile(new File([], 'vacio.mp4', { type: 'video/mp4' }));
    expect(result).toEqual({ ok: false, message: 'El archivo está vacío.' });
  });
});

describe('formatAttachmentDuration', () => {
  it('muestra minutos y segundos con dos dígitos', () => {
    expect(formatAttachmentDuration(0)).toBe('0:00');
    expect(formatAttachmentDuration(9)).toBe('0:09');
    expect(formatAttachmentDuration(75)).toBe('1:15');
  });
});
