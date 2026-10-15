import { ImageIcon } from '@phosphor-icons/react';
import { useState } from 'react';
import { isAllowedImageUrl } from './markdownPolicy';
import styles from './styles.module.css';

type ExternalImageProps = {
  src?: string;
  alt?: string;
  title?: string;
};

function ExternalImage({ src, alt = '', title }: ExternalImageProps) {
  const [failed, setFailed] = useState(false);

  if (!isAllowedImageUrl(src) || failed) {
    return (
      <span
        className={styles.placeholder}
        role='img'
        aria-label={alt ? `Imagem indisponível: ${alt}` : 'Imagem indisponível'}
      >
        <ImageIcon size={28} aria-hidden='true' />
        <span>Imagem indisponível</span>
        {alt && <span className={styles.alternativeText}>{alt}</span>}
      </span>
    );
  }

  // A span is valid inside the paragraph generated for a Markdown image.
  // Do not spread attributes from user content: only these fixed props are used.
  return (
    <span className={styles.imageWrapper}>
      <img
        className={styles.image}
        src={src}
        alt={alt}
        title={title}
        loading='lazy'
        decoding='async'
        referrerPolicy='no-referrer'
        onError={() => setFailed(true)}
      />
    </span>
  );
}

export default ExternalImage;
