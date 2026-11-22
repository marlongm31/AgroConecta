import { PageHeader } from '../components/ui/Display';
import { CatalogView } from '../components/catalog/CatalogView';

export function CatalogPage() {
  return (
    <>
      <PageHeader
        title="Catálogo de productos"
        subtitle="Explora los productos frescos publicados por los productores de la región."
      />
      <CatalogView />
    </>
  );
}
