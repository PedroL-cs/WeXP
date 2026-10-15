import { useEffect, useId, useRef, useState, type FormEvent } from 'react';
import { createPortal } from 'react-dom';
import {
  extractYouTubeVideoId,
  getYouTubeWatchUrl,
} from '../MarkdownRenderer/youtubePolicy';
import styles from './styles.module.css';

type YouTubeValues = { videoId: string; title: string };

type YouTubeDialogProps = {
  initial?: Partial<YouTubeValues> & { url?: string };
  editing?: boolean;
  save: (values: YouTubeValues) => void;
  close: () => void;
};

export default function YouTubeDialog({
  initial = {},
  editing = false,
  save,
  close,
}: YouTubeDialogProps) {
  const [url, setUrl] = useState(
    initial.url || (initial.videoId ? getYouTubeWatchUrl(initial.videoId) : ''),
  );
  const [title, setTitle] = useState(initial.title ?? '');
  const [errors, setErrors] = useState({ url: '', title: '' });
  const dialog = useRef<HTMLDialogElement>(null);
  const input = useRef<HTMLInputElement>(null);
  const id = useId();

  useEffect(() => {
    const element = dialog.current;
    if (element && !element.open) element.showModal();
    input.current?.focus();
    return () => {
      if (element?.open) element.close();
    };
  }, []);

  function submit(event: FormEvent) {
    event.preventDefault();
    event.stopPropagation();
    const videoId = extractYouTubeVideoId(url);
    const cleanTitle = title.trim();
    const nextErrors = {
      url: videoId ? '' : 'Use uma URL HTTPS válida de um vídeo do YouTube.',
      title: cleanTitle
        ? ''
        : 'Informe um título que descreva o conteúdo do vídeo.',
    };
    setErrors(nextErrors);
    if (!videoId || !cleanTitle) {
      if (!videoId) input.current?.focus();
      return;
    }
    save({ videoId, title: cleanTitle });
  }

  return createPortal(
    <dialog
      ref={dialog}
      className={styles.imageDialog}
      aria-labelledby={`${id}-title`}
      aria-describedby={`${id}-help`}
      onCancel={event => {
        event.preventDefault();
        close();
      }}
    >
      <h2 id={`${id}-title`}>
        {editing ? 'Editar vídeo' : 'Inserir vídeo do YouTube'}
      </h2>
      <p id={`${id}-help`}>
        Cole uma URL de vídeo do YouTube. O player será carregado somente quando
        o leitor solicitar.
      </p>
      <form onSubmit={submit}>
        <label htmlFor={`${id}-url`}>URL do vídeo</label>
        <input
          id={`${id}-url`}
          ref={input}
          value={url}
          required
          onChange={event => {
            setUrl(event.target.value);
            setErrors(current => ({ ...current, url: '' }));
          }}
          placeholder='https://www.youtube.com/watch?v=...'
          aria-invalid={Boolean(errors.url)}
          aria-describedby={errors.url ? `${id}-url-error` : `${id}-help`}
        />
        {errors.url && (
          <p id={`${id}-url-error`} className={styles.imageError} role='alert'>
            {errors.url}
          </p>
        )}
        <label htmlFor={`${id}-video-title`}>Título descritivo</label>
        <input
          id={`${id}-video-title`}
          value={title}
          required
          maxLength={160}
          onChange={event => {
            setTitle(event.target.value);
            setErrors(current => ({ ...current, title: '' }));
          }}
          placeholder='Ex.: Estratégia para derrotar o chefe final'
          aria-invalid={Boolean(errors.title)}
          aria-describedby={errors.title ? `${id}-title-error` : undefined}
        />
        {errors.title && (
          <p id={`${id}-title-error`} className={styles.imageError} role='alert'>
            {errors.title}
          </p>
        )}
        <div className={styles.imageDialogActions}>
          <button type='button' onClick={close}>
            Cancelar
          </button>
          <button type='submit'>
            {editing ? 'Salvar vídeo' : 'Inserir vídeo'}
          </button>
        </div>
      </form>
    </dialog>,
    document.body,
  );
}
