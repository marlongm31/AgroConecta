export const MAX_CHAT_ATTACHMENT_BYTES = 5 * 1024 * 1024;

export interface ValidatedChatAttachment {
  name: string;
  contentType: string;
  sizeBytes: number;
}

export type ChatAttachmentValidation =
  | { ok: true; value: ValidatedChatAttachment }
  | { ok: false; message: string };

const EXTENSIONS_BY_TYPE: Record<string, string[]> = {
  'image/jpeg': ['jpg', 'jpeg'],
  'image/png': ['png'],
  'image/webp': ['webp'],
  'application/pdf': ['pdf'],
  'application/msword': ['doc'],
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['docx'],
  'application/vnd.ms-excel': ['xls'],
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['xlsx'],
  'video/mp4': ['mp4'],
  'audio/mp4': ['m4a', 'mp4'],
  'video/webm': ['webm'],
  'audio/webm': ['webm'],
  'audio/ogg': ['ogg'],
  'audio/wav': ['wav'],
  'audio/mpeg': ['mp3'],
};

export async function validateChatFile(file: File): Promise<ChatAttachmentValidation> {
  if (file.size === 0) {
    return { ok: false, message: 'El archivo está vacío.' };
  }
  if (file.size > MAX_CHAT_ATTACHMENT_BYTES) {
    return { ok: false, message: 'El archivo supera el tamaño máximo de 5 MB.' };
  }

  const head = await readBytes(file.slice(0, 12));
  const contentType = await detectContentType(head, file);
  if (!contentType) {
    return {
      ok: false,
      message: 'Formato no permitido. Usa JPG, PNG, WEBP, PDF, Word, Excel, MP4, WebM, WAV, OGG, MP3 o M4A.',
    };
  }

  const extension = fileNameExtension(file.name);
  if (!EXTENSIONS_BY_TYPE[contentType].includes(extension)) {
    return { ok: false, message: 'La extensión del archivo no coincide con su contenido.' };
  }

  return {
    ok: true,
    value: {
      name: file.name.length > 120 ? file.name.slice(0, 120) : file.name,
      contentType,
      sizeBytes: file.size,
    },
  };
}

export function formatAttachmentSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function formatAttachmentDuration(seconds: number): string {
  const safe = Math.max(0, Math.floor(seconds));
  const minutes = Math.floor(safe / 60);
  const remainder = safe % 60;
  return `${minutes}:${remainder.toString().padStart(2, '0')}`;
}

async function detectContentType(head: Uint8Array, file: File): Promise<string | null> {
  if (isJpeg(head)) return 'image/jpeg';
  if (isPng(head)) return 'image/png';
  if (isWebp(head)) return 'image/webp';
  if (startsWith(head, ascii('%PDF-'))) return 'application/pdf';
  if (isOle(head)) {
    const extension = fileNameExtension(file.name);
    if (extension === 'doc') return 'application/msword';
    if (extension === 'xls') return 'application/vnd.ms-excel';
    return null;
  }
  if (isZip(head)) {
    return await detectOfficeContentType(file);
  }
  if (isIsoBmff(head)) {
    if (fileNameExtension(file.name) === 'm4a' || file.type.startsWith('audio/')) return 'audio/mp4';
    return 'video/mp4';
  }
  if (isWebM(head)) {
    return file.type.startsWith('audio/') ? 'audio/webm' : 'video/webm';
  }
  if (startsWith(head, ascii('OggS'))) return 'audio/ogg';
  if (isWave(head)) return 'audio/wav';
  if (isMp3(head)) return 'audio/mpeg';
  return null;
}

async function readBytes(blob: Blob): Promise<Uint8Array> {
  if (typeof blob.arrayBuffer === 'function') {
    return new Uint8Array(await blob.arrayBuffer());
  }
  return new Promise<Uint8Array>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(new Uint8Array(reader.result as ArrayBuffer));
    reader.onerror = () => reject(reader.error ?? new Error('No se pudo leer el archivo'));
    reader.readAsArrayBuffer(blob);
  });
}

function fileNameExtension(name: string): string {
  const separator = name.lastIndexOf('.');
  return separator < 0 || separator === name.length - 1
    ? ''
    : name.slice(separator + 1).toLowerCase();
}

function ascii(value: string): number[] {
  return value.split('').map((character) => character.charCodeAt(0));
}

function isJpeg(bytes: Uint8Array): boolean {
  return bytes.length >= 3 && bytes[0] === 0xFF && bytes[1] === 0xD8 && bytes[2] === 0xFF;
}

function isPng(bytes: Uint8Array): boolean {
  const signature = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A];
  return startsWith(bytes, signature);
}

function isWebp(bytes: Uint8Array): boolean {
  return bytes.length >= 12
    && String.fromCharCode(...bytes.slice(0, 4)) === 'RIFF'
    && String.fromCharCode(...bytes.slice(8, 12)) === 'WEBP';
}

function isIsoBmff(bytes: Uint8Array): boolean {
  return bytes.length >= 8
    && bytes[4] === 0x66 && bytes[5] === 0x74 && bytes[6] === 0x79 && bytes[7] === 0x70;
}

function isWebM(bytes: Uint8Array): boolean {
  return startsWith(bytes, [0x1A, 0x45, 0xDF, 0xA3]);
}

function isWave(bytes: Uint8Array): boolean {
  return bytes.length >= 12
    && String.fromCharCode(...bytes.slice(0, 4)) === 'RIFF'
    && String.fromCharCode(...bytes.slice(8, 12)) === 'WAVE';
}

function isMp3(bytes: Uint8Array): boolean {
  if (startsWith(bytes, ascii('ID3'))) return true;
  return bytes.length >= 2 && bytes[0] === 0xFF && (bytes[1] & 0xE0) === 0xE0;
}

function isOle(bytes: Uint8Array): boolean {
  return startsWith(bytes, [0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1]);
}

function isZip(bytes: Uint8Array): boolean {
  return startsWith(bytes, [0x50, 0x4B, 0x03, 0x04]);
}

function startsWith(bytes: Uint8Array, signature: number[]): boolean {
  if (bytes.length < signature.length) return false;
  return signature.every((value, index) => bytes[index] === value);
}

async function detectOfficeContentType(file: File): Promise<string | null> {
  const bytes = await readBytes(file.slice(0, 8192));
  const text = new TextDecoder('iso-8859-1').decode(bytes);
  if (!text.includes('[Content_Types].xml')) return null;
  if (text.includes('word/')) return 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
  if (text.includes('xl/')) return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
  return null;
}
