import { BookmarkSimpleIcon } from '@phosphor-icons/react';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router';
import {
  getSavedStatus,
  removeSavedItem,
  saveItem,
} from '../../api/savedItems';
import { useAuth } from '../../contexts/AuthContext';
import type { SavedResourceType } from '../../types/SavedItem';
import styles from './styles.module.css';

type SaveButtonProps = {
  type: SavedResourceType;
  resourceId: string;
  compact?: boolean;
  initialSaved?: boolean;
  onSavedChange?: (saved: boolean) => void;
};

export default function SaveButton({
  type,
  resourceId,
  compact = false,
  initialSaved,
  onSavedChange,
}: SaveButtonProps) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [saved, setSaved] = useState(initialSaved ?? false);
  const [loading, setLoading] = useState(
    isAuthenticated && initialSaved === undefined,
  );
  const [error, setError] = useState('');

  useEffect(() => {
    setError('');
    if (!isAuthenticated || initialSaved !== undefined) {
      setSaved(initialSaved ?? false);
      setLoading(false);
      return;
    }

    const controller = new AbortController();
    setLoading(true);
    getSavedStatus(type, resourceId, controller.signal)
      .then(status => {
        if (!controller.signal.aborted) setSaved(status.saved);
      })
      .catch(() => {
        if (!controller.signal.aborted)
          setError('Não foi possível verificar este item.');
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [initialSaved, isAuthenticated, resourceId, type]);

  async function toggleSaved(event: React.MouseEvent<HTMLButtonElement>) {
    event.preventDefault();
    event.stopPropagation();

    if (!isAuthenticated) {
      navigate('/login', {
        state: { from: `${location.pathname}${location.search}` },
      });
      return;
    }

    setLoading(true);
    setError('');
    try {
      const next = !saved;
      if (next) await saveItem(type, resourceId);
      else await removeSavedItem(type, resourceId);
      setSaved(next);
      onSavedChange?.(next);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : 'Não foi possível atualizar este item.',
      );
    } finally {
      setLoading(false);
    }
  }

  const label = loading
    ? 'Atualizando…'
    : saved
      ? 'Remover dos salvos'
      : 'Salvar';

  return (
    <button
      type='button'
      className={`${styles.button} ${saved ? styles.saved : ''} ${compact ? styles.compact : ''}`}
      onClick={toggleSaved}
      disabled={loading}
      aria-pressed={saved}
      aria-label={label}
      aria-busy={loading}
      title={error || label}
    >
      <BookmarkSimpleIcon size={compact ? 18 : 20} weight={saved ? 'fill' : 'bold'} />
      {!compact && <span>{label}</span>}
    </button>
  );
}
