import { ImagePlus, Trash2 } from 'lucide-react';
import { useEffect, useState, type ChangeEvent } from 'react';
import { Button } from '../ui/Button';
import { PageLoader } from '../ui/Feedback';
import { productService } from '../../services/productService';
import { useOffline } from '../../context/OfflineContext';
import { getErrorMessage } from '../../lib/errors';
import type { ProductImage } from '../../types';
import { assetUrl } from '../../lib/assetUrl';

const MAX_BYTES = 5 * 1024 * 1024;
const MAX_IMAGES = 6;
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

export function ProductImageManager({ productId, onChanged }: { productId: number; onChanged?: () => void }) {
  const { online } = useOffline();
  const [images, setImages] = useState<ProductImage[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    productService
      .listImages(productId)
      .then((items) => {
        if (active) setImages(items);
      })
      .catch((caught) => {
        if (active) setError(getErrorMessage(caught));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [productId]);

  const handleUpload = async (event: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? []);
    event.target.value = '';
    if (files.length === 0) return;
    setError('');
    if (!online) {
      setError('Las fotografías requieren conexión a internet. Se podrán subir cuando vuelva la conexión.');
      return;
    }
    if (images.length + files.length > MAX_IMAGES) {
      setError(`Un producto puede tener como máximo ${MAX_IMAGES} fotografías.`);
      return;
    }
    const invalid = files.find((file) => !ALLOWED_TYPES.includes(file.type) || file.size > MAX_BYTES);
    if (invalid) {
      setError('Usa imágenes JPG, PNG o WEBP de máximo 5 MB cada una.');
      return;
    }
    setBusy(true);
    try {
      for (const file of files) {
        const created = await productService.uploadImage(productId, file);
        setImages((current) => [...current, created]);
      }
      onChanged?.();
    } catch (caught) {
      setError(getErrorMessage(caught));
    } finally {
      setBusy(false);
    }
  };

  const handleRemove = async (imageId: number) => {
    setBusy(true);
    setError('');
    try {
      await productService.removeImage(imageId);
      setImages((current) => current.filter((image) => image.id !== imageId));
      onChanged?.();
    } catch (caught) {
      setError(getErrorMessage(caught));
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="rounded-xl border border-slate-200 p-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-sm font-semibold text-slate-800">Fotografías de la cosecha</h3>
          <p className="mt-1 text-xs text-slate-500">JPG, PNG o WEBP · máximo 5 MB por imagen · hasta {MAX_IMAGES} imágenes</p>
        </div>
        <label className="inline-flex cursor-pointer items-center gap-2 rounded-lg bg-agro-600 px-3 py-2 text-sm font-medium text-white hover:bg-agro-700">
          <ImagePlus className="h-4 w-4" />
          Agregar fotografías
          <input
            type="file"
            accept={ALLOWED_TYPES.join(',')}
            multiple
            className="hidden"
            onChange={handleUpload}
            disabled={busy || images.length >= MAX_IMAGES}
          />
        </label>
      </div>

      {error ? <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
      {loading ? (
        <div className="mt-4">
          <PageLoader />
        </div>
      ) : images.length === 0 ? (
        <p className="mt-4 rounded-lg bg-slate-50 px-3 py-4 text-center text-sm text-slate-500">
          Aún no hay fotografías. Las imágenes reales ayudan a generar confianza en los compradores.
        </p>
      ) : (
        <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
          {images.map((image) => (
            <div key={image.id} className="relative overflow-hidden rounded-lg border border-slate-200">
              <img src={assetUrl(image.imageUrl)} alt={`Fotografía ${image.position + 1} del producto`} className="h-28 w-full object-cover" />
              <Button
                variant="ghost"
                className="absolute right-1 top-1 bg-white/90 text-red-600"
                icon={<Trash2 className="h-4 w-4" />}
                onClick={() => void handleRemove(image.id)}
                disabled={busy}
                aria-label={`Eliminar fotografía ${image.position + 1}`}
              >
                <span className="sr-only">Eliminar</span>
              </Button>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
