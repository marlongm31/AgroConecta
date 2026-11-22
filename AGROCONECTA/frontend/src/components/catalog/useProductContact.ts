import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useOffline } from '../../context/OfflineContext';
import { useToast } from '../../context/ToastContext';
import { chatService } from '../../services/chatService';
import { getErrorMessage } from '../../lib/errors';
import { getProductStatus } from '../../utils/productStatus';
import type { Product } from '../../types';

export function useProductContact() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const { online } = useOffline();
  const toast = useToast();
  const [startingProductId, setStartingProductId] = useState<number | null>(null);

  const canContactProduct = (product: Product) =>
    getProductStatus(product) === 'active' && product.ownerId !== user?.id;

  const contactLabel = user ? 'Contactar por chat' : 'Inicia sesión para contactar';

  const contact = async (product: Product) => {
    if (!user) {
      navigate('/iniciar-sesion', { state: { from: location.pathname } });
      return;
    }
    if (!online) {
      toast.error('Necesitas conexión para iniciar una conversación.');
      return;
    }

    setStartingProductId(product.id);
    try {
      const conversation = await chatService.start(product.id);
      navigate(`/mensajes?conversacion=${conversation.id}`);
    } catch (caught) {
      toast.error(getErrorMessage(caught));
    } finally {
      setStartingProductId(null);
    }
  };

  return { contact, startingProductId, canContactProduct, contactLabel };
}
