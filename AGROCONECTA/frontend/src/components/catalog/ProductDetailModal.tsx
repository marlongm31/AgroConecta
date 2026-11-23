import { useEffect, useState } from 'react';
import { ChevronLeft, ChevronRight, Mail, MapPin, MessageCircle, Phone } from 'lucide-react';
import { Badge } from '../ui/Display';
import { Button } from '../ui/Button';
import { Modal } from '../ui/Modal';
import type { Product } from '../../types';
import { MEASUREMENT_UNIT_LABELS, PRODUCT_CATEGORY_LABELS } from '../../utils/labels';
import { formatCurrency, formatDate, formatNumber } from '../../utils/format';
import { getProductStatus, PRODUCT_STATUS_META } from '../../utils/productStatus';
import { assetUrl } from '../../lib/assetUrl';

interface ProductDetailModalProps {
  product: Product | null;
  open: boolean;
  onClose: () => void;
  onContact?: (product: Product) => void;
  contactLabel?: string;
  contactLoading?: boolean;
}

export function ProductDetailModal({
  product,
  open,
  onClose,
  onContact,
  contactLabel = 'Contactar por chat',
  contactLoading = false,
}: ProductDetailModalProps) {
  const [imageIndex, setImageIndex] = useState(0);
  const images = product?.imageUrls ?? [];

  useEffect(() => {
    setImageIndex(0);
  }, [product?.id]);

  useEffect(() => {
    if (imageIndex >= images.length) setImageIndex(0);
  }, [imageIndex, images.length]);

  if (!product || !open) return null;

  const status = getProductStatus(product);

  return (
    <Modal
      open
      title={product.name}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Cerrar
          </Button>
          {onContact ? (
            <Button
              icon={<MessageCircle className="h-4 w-4" />}
              loading={contactLoading}
              onClick={() => onContact(product)}
            >
              {contactLabel}
            </Button>
          ) : null}
        </>
      }
    >
      {images.length > 0 ? (
        <div>
          <div className="relative flex h-56 items-center justify-center overflow-hidden rounded-xl bg-slate-50">
            <img
              src={assetUrl(images[imageIndex])}
              alt={`Fotografía ${imageIndex + 1} de ${product.name}`}
              className="h-full w-full object-contain p-2"
            />
            {images.length > 1 ? (
              <>
                <button
                  type="button"
                  aria-label="Fotografía anterior"
                  onClick={() => setImageIndex((current) => (current - 1 + images.length) % images.length)}
                  className="absolute left-2 top-1/2 -translate-y-1/2 rounded-full bg-white/90 p-1.5 text-slate-700 shadow-sm hover:bg-white"
                >
                  <ChevronLeft className="h-5 w-5" />
                </button>
                <button
                  type="button"
                  aria-label="Fotografía siguiente"
                  onClick={() => setImageIndex((current) => (current + 1) % images.length)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 rounded-full bg-white/90 p-1.5 text-slate-700 shadow-sm hover:bg-white"
                >
                  <ChevronRight className="h-5 w-5" />
                </button>
              </>
            ) : null}
          </div>
          {images.length > 1 ? (
            <div className="mt-2 flex gap-2 overflow-x-auto">
              {images.map((image, index) => (
                <button
                  key={`${image}-${index}`}
                  type="button"
                  aria-label={`Ver fotografía ${index + 1}`}
                  onClick={() => setImageIndex(index)}
                  className={`h-14 w-20 shrink-0 overflow-hidden rounded-lg border-2 bg-slate-50 ${
                    index === imageIndex ? 'border-agro-600' : 'border-transparent'
                  }`}
                >
                  <img src={assetUrl(image)} alt="" className="h-full w-full object-contain p-1" />
                </button>
              ))}
            </div>
          ) : null}
        </div>
      ) : null}

      <div className={`flex flex-wrap gap-2 ${images.length > 0 ? 'mt-4' : ''}`}>
        <Badge tone="green">{PRODUCT_CATEGORY_LABELS[product.category]}</Badge>
        {status !== 'active' ? (
          <Badge tone={PRODUCT_STATUS_META[status].tone}>{PRODUCT_STATUS_META[status].label}</Badge>
        ) : null}
      </div>

      <p className="mt-3 text-sm leading-relaxed text-slate-600">{product.description}</p>

      {product.characteristics ? (
        <div className="mt-4">
          <h3 className="text-sm font-semibold text-slate-800">Características</h3>
          <p className="mt-1 text-sm text-slate-600">{product.characteristics}</p>
        </div>
      ) : null}

      <dl className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
        <div className="rounded-xl bg-slate-50 p-3">
          <dt className="text-xs text-slate-400">Precio</dt>
          <dd className="mt-1 font-semibold text-agro-700">{formatCurrency(product.price)}</dd>
        </div>
        <div className="rounded-xl bg-slate-50 p-3">
          <dt className="text-xs text-slate-400">Disponible</dt>
          <dd className="mt-1 font-semibold text-slate-800">
            {formatNumber(product.availableQuantity)} {MEASUREMENT_UNIT_LABELS[product.unit].toLowerCase()}
          </dd>
        </div>
        <div className="rounded-xl bg-slate-50 p-3">
          <dt className="text-xs text-slate-400">Publicado</dt>
          <dd className="mt-1 font-semibold text-slate-800">{product.createdAt ? formatDate(product.createdAt) : '—'}</dd>
        </div>
      </dl>

      <div className="mt-4 rounded-xl border border-slate-100 p-4">
        <h3 className="text-sm font-semibold text-slate-800">Datos del productor</h3>
        <div className="mt-2 space-y-1 text-sm text-slate-600">
          <p className="font-medium text-slate-700">{product.producerName ?? 'Productor de AgroConecta'}</p>
          {product.producerLocation ? (
            <p className="flex items-center gap-2"><MapPin className="h-4 w-4 text-slate-400" />{product.producerLocation}</p>
          ) : null}
          <p className="flex items-center gap-2"><Phone className="h-4 w-4 text-slate-400" />{product.contactPhone}</p>
          <p className="flex items-center gap-2"><Mail className="h-4 w-4 text-slate-400" />{product.contactEmail}</p>
        </div>
      </div>
    </Modal>
  );
}
