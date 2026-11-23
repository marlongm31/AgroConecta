import { Eye, MessageCircle, Store } from 'lucide-react';
import { Badge } from '../ui/Display';
import { Button } from '../ui/Button';
import type { Product } from '../../types';
import { PRODUCT_CATEGORY_LABELS } from '../../utils/labels';
import { formatCurrency, formatNumber } from '../../utils/format';
import { getProductStatus, PRODUCT_STATUS_META } from '../../utils/productStatus';
import { assetUrl } from '../../lib/assetUrl';

interface ProductCardProps {
  product: Product;
  onViewDetails: (product: Product) => void;
  onContact?: (product: Product) => void;
  contactLabel?: string;
  contactLoading?: boolean;
}

export function ProductCard({
  product,
  onViewDetails,
  onContact,
  contactLabel = 'Contactar por chat',
  contactLoading = false,
}: ProductCardProps) {
  const status = getProductStatus(product);

  return (
    <article className="card flex flex-col overflow-hidden">
      {product.imageUrls?.[0] ? (
        <img src={assetUrl(product.imageUrls[0])} alt={`Fotografía de ${product.name}`} className="h-40 w-full object-cover" />
      ) : (
        <div className="flex h-40 w-full items-center justify-center bg-slate-100 text-slate-400">
          <Store className="h-9 w-9" />
        </div>
      )}
      <div className="flex flex-1 flex-col p-5">
        <div className="flex flex-wrap items-center gap-2">
          <Badge tone="green">{PRODUCT_CATEGORY_LABELS[product.category]}</Badge>
          {status !== 'active' ? (
            <Badge tone={PRODUCT_STATUS_META[status].tone}>{PRODUCT_STATUS_META[status].label}</Badge>
          ) : null}
        </div>
        <h3 className="mt-3 text-base font-semibold text-slate-900">{product.name}</h3>
        <p className="mt-1 line-clamp-3 flex-1 text-sm text-slate-500">{product.description}</p>
        <div className="mt-3 flex items-center justify-between gap-2">
          <span className="text-lg font-bold text-agro-700">{formatCurrency(product.price)}</span>
          <span className="text-xs text-slate-400">
            {formatNumber(product.availableQuantity)} disponibles
          </span>
        </div>
        <div className="mt-4 border-t border-slate-100 pt-3 text-xs text-slate-500">
          <p className="font-medium text-slate-700">{product.producerName ?? 'Productor de AgroConecta'}</p>
          {product.producerLocation ? <p>{product.producerLocation}</p> : null}
        </div>
        <div className="mt-3 flex flex-col gap-2">
          <Button
            variant="secondary"
            className="w-full justify-center"
            icon={<Eye className="h-4 w-4" />}
            onClick={() => onViewDetails(product)}
          >
            Ver detalles
          </Button>
          {onContact ? (
            <Button
              className="w-full justify-center"
              icon={<MessageCircle className="h-4 w-4" />}
              loading={contactLoading}
              onClick={() => onContact(product)}
            >
              {contactLabel}
            </Button>
          ) : null}
        </div>
      </div>
    </article>
  );
}
