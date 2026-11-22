import { Search, Store } from 'lucide-react';
import { useEffect, useState } from 'react';
import { productService } from '../../services/productService';
import { EmptyState, PageLoader } from '../ui/Feedback';
import { Button } from '../ui/Button';
import { SelectField, TextField } from '../ui/FormFields';
import { ProductCard } from './ProductCard';
import { ProductDetailModal } from './ProductDetailModal';
import { useProductContact } from './useProductContact';
import { PRODUCT_CATEGORY_OPTIONS } from '../../utils/labels';
import { getErrorMessage } from '../../lib/errors';
import type { Product, ProductCategory } from '../../types';

export function CatalogView() {
  const { contact, startingProductId, canContactProduct, contactLabel } = useProductContact();
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [search, setSearch] = useState('');
  const [appliedSearch, setAppliedSearch] = useState('');
  const [category, setCategory] = useState<ProductCategory | ''>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    productService
      .catalog({ search: appliedSearch, category })
      .then((page) => {
        if (active) setProducts(page.content);
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
  }, [appliedSearch, category]);

  const handleSearch = (event: React.FormEvent) => {
    event.preventDefault();
    setAppliedSearch(search.trim());
  };

  const selectedCanContact = selectedProduct ? canContactProduct(selectedProduct) : false;

  return (
    <div>
      <form onSubmit={handleSearch} className="mb-6 grid gap-3 sm:grid-cols-[1fr_220px_auto]">
        <TextField
          label="Buscar producto"
          name="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Tomate, café, aguacate..."
        />
        <SelectField
          label="Categoría"
          name="category"
          value={category}
          onChange={(event) => setCategory(event.target.value as ProductCategory | '')}
          options={PRODUCT_CATEGORY_OPTIONS}
          placeholder="Todas las categorías"
        />
        <div className="flex items-end">
          <Button type="submit" icon={<Search className="h-4 w-4" />} className="w-full sm:w-auto">
            Buscar
          </Button>
        </div>
      </form>

      {loading ? (
        <PageLoader />
      ) : error ? (
        <EmptyState icon={<Store className="h-8 w-8" />} title="No fue posible cargar el catálogo" description={error} />
      ) : products.length === 0 ? (
        <EmptyState
          icon={<Store className="h-8 w-8" />}
          title="No encontramos productos"
          description="Prueba con otra búsqueda o cambia la categoría."
        />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {products.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onViewDetails={setSelectedProduct}
              onContact={canContactProduct(product) ? contact : undefined}
              contactLabel={contactLabel}
              contactLoading={startingProductId === product.id}
            />
          ))}
        </div>
      )}

      <ProductDetailModal
        product={selectedProduct}
        open={Boolean(selectedProduct)}
        onClose={() => setSelectedProduct(null)}
        onContact={selectedCanContact ? contact : undefined}
        contactLabel={contactLabel}
        contactLoading={Boolean(selectedProduct && startingProductId === selectedProduct.id)}
      />
    </div>
  );
}
