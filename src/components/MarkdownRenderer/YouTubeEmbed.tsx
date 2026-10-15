import { ArrowSquareOutIcon, PlayIcon, YoutubeLogoIcon } from '@phosphor-icons/react';
import { useState } from 'react';
import {
  getYouTubeEmbedUrl,
  getYouTubeWatchUrl,
  isValidYouTubeVideoId,
} from './youtubePolicy';
import styles from './styles.module.css';

type YouTubeEmbedProps = {
  videoId?: string;
  title?: string;
};

export default function YouTubeEmbed({ videoId, title }: YouTubeEmbedProps) {
  const [loaded, setLoaded] = useState(false);
  const valid = isValidYouTubeVideoId(videoId) && Boolean(title?.trim());

  if (!valid) {
    return (
      <div className={styles.videoUnavailable} role='status'>
        <YoutubeLogoIcon size={30} aria-hidden='true' />
        <span>Vídeo do YouTube inválido ou indisponível.</span>
      </div>
    );
  }

  const accessibleTitle = title!.trim();

  return (
    <div className={styles.videoBlock}>
      <div className={styles.videoFrame}>
        {loaded ? (
          <iframe
            src={getYouTubeEmbedUrl(videoId)}
            title={accessibleTitle}
            loading='lazy'
            allow='accelerometer; autoplay; encrypted-media; picture-in-picture'
            referrerPolicy='strict-origin-when-cross-origin'
            allowFullScreen
          />
        ) : (
          <button
            type='button'
            className={styles.videoConsent}
            onClick={() => setLoaded(true)}
            aria-label={`Carregar vídeo do YouTube: ${accessibleTitle}`}
          >
            <span className={styles.videoPlay} aria-hidden='true'>
              <PlayIcon size={28} weight='fill' />
            </span>
            <span className={styles.videoTitle}>{accessibleTitle}</span>
            <span className={styles.videoHint}>
              Clique para carregar conteúdo do YouTube
            </span>
          </button>
        )}
      </div>
      <a
        className={styles.videoLink}
        href={getYouTubeWatchUrl(videoId)}
        target='_blank'
        rel='noopener noreferrer'
      >
        Abrir no YouTube
        <ArrowSquareOutIcon size={14} aria-hidden='true' />
      </a>
    </div>
  );
}
