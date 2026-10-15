import {
  $isImageNode,
  activeEditor$,
  closeImageDialog$,
  imageDialogState$,
  lexical,
  saveImage$,
} from '@mdxeditor/editor';
import { useCellValue, usePublisher } from '@mdxeditor/gurx';
import { useEffect, useId, useRef, useState, type FormEvent } from 'react';
import { createPortal } from 'react-dom';
import { isAllowedImageUrl } from '../MarkdownRenderer/markdownPolicy';
import { normalizeImageUrl } from './imageMarkdown';
import styles from './styles.module.css';

type ImageValues = { src: string; altText: string; title: string };

function ImageForm({
  initial,
  editing,
  save,
  close,
}: {
  initial: ImageValues;
  editing: boolean;
  save: (values: ImageValues) => void;
  close: () => void;
}) {
  const [values, setValues] = useState(initial);
  const [error, setError] = useState('');
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
    const src = normalizeImageUrl(values.src);
    if (!isAllowedImageUrl(src)) {
      setError(
        'Use uma URL HTTPS pública que aponte diretamente para uma imagem.',
      );
      input.current?.focus();
      return;
    }
    save({ src, altText: values.altText.trim(), title: values.title.trim() });
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
        {editing ? 'Editar imagem' : 'Inserir imagem por URL'}
      </h2>
      <p id={`${id}-help`}>
        Use uma URL HTTPS pública e permanente que aponte diretamente para uma
        imagem.
      </p>
      <form onSubmit={submit}>
        <label htmlFor={`${id}-src`}>URL pública da imagem</label>
        <input
          id={`${id}-src`}
          ref={input}
          value={values.src}
          required
          onChange={event => {
            setValues({ ...values, src: event.target.value });
            setError('');
          }}
          placeholder='https://exemplo.com/imagem.webp'
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : `${id}-help`}
        />
        {error && (
          <p id={`${id}-error`} className={styles.imageError} role='alert'>
            {error}
          </p>
        )}
        <label htmlFor={`${id}-alt`}>Texto alternativo</label>
        <input
          id={`${id}-alt`}
          value={values.altText}
          onChange={event =>
            setValues({ ...values, altText: event.target.value })
          }
          placeholder='Descreva a imagem para quem não consegue vê-la'
        />
        <label htmlFor={`${id}-caption`}>Título opcional</label>
        <input
          id={`${id}-caption`}
          value={values.title}
          onChange={event =>
            setValues({ ...values, title: event.target.value })
          }
        />
        <div className={styles.imageDialogActions}>
          <button type='button' onClick={close}>
            Cancelar
          </button>
          <button type='submit'>
            {editing ? 'Salvar imagem' : 'Inserir imagem'}
          </button>
        </div>
      </form>
    </dialog>,
    document.body,
  );
}

export default function ImageUrlDialog() {
  const state = useCellValue(imageDialogState$);
  const editor = useCellValue(activeEditor$);
  const save = usePublisher(saveImage$);
  const close = usePublisher(closeImageDialog$);
  if (state.type === 'inactive') return null;
  let initial: Partial<ImageValues> =
    state.type === 'editing' ? state.initialValues : {};
  if (state.type === 'editing' && editor) {
    const nodeKey = state.nodeKey;
    const current = editor.getEditorState().read(() => {
      const node = lexical.$getNodeByKey(nodeKey);
      return $isImageNode(node)
        ? {
            src: node.getSrc(),
            altText: node.getAltText(),
            title: node.getTitle() ?? '',
          }
        : null;
    });
    if (current) initial = current;
  }
  return (
    <ImageForm
      key={state.type === 'editing' ? state.nodeKey : 'new'}
      initial={{
        src: initial.src ?? '',
        altText: initial.altText ?? '',
        title: initial.title ?? '',
      }}
      editing={state.type === 'editing'}
      save={save}
      close={() => close()}
    />
  );
}
