const DB_NAME = 'agroconecta-attachments';
const STORE_NAME = 'attachments';
const DB_VERSION = 1;

function openDatabase(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    if (typeof indexedDB === 'undefined') {
      reject(new Error('El navegador no permite guardar archivos sin conexión.'));
      return;
    }
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = () => {
      const database = request.result;
      if (!database.objectStoreNames.contains(STORE_NAME)) {
        database.createObjectStore(STORE_NAME);
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error ?? new Error('No fue posible abrir el almacenamiento de archivos.'));
  });
}

async function runRequest<T>(mode: IDBTransactionMode, action: (store: IDBObjectStore) => IDBRequest<T>): Promise<T> {
  const database = await openDatabase();
  return new Promise<T>((resolve, reject) => {
    const transaction = database.transaction(STORE_NAME, mode);
    const request = action(transaction.objectStore(STORE_NAME));
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error ?? new Error('No fue posible acceder al archivo guardado.'));
    transaction.oncomplete = () => database.close();
    transaction.onerror = () => database.close();
  });
}

export async function saveAttachment(id: string, file: File): Promise<void> {
  await runRequest('readwrite', (store) => store.put(file, id));
}

export async function getAttachment(id: string): Promise<File | null> {
  const file = await runRequest<File | undefined>('readonly', (store) => store.get(id));
  return file ?? null;
}

export async function deleteAttachment(id: string): Promise<void> {
  await runRequest('readwrite', (store) => store.delete(id));
}
