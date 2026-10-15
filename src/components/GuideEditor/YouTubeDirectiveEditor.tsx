import {
  PencilSimpleIcon,
  TrashIcon,
  YoutubeLogoIcon,
} from '@phosphor-icons/react';
import {
  type DirectiveEditorProps,
  useLexicalNodeRemove,
  useMdastNodeUpdater,
} from '@mdxeditor/editor';
import type { LeafDirective } from 'mdast-util-directive';
import { useState } from 'react';
import {
  getYouTubeWatchUrl,
  isValidYouTubeVideoId,
} from '../MarkdownRenderer/youtubePolicy';
import YouTubeDialog from './YouTubeDialog';
import styles from './styles.module.css';

type YouTubeValues = { videoId: string; title: string };

export default function YouTubeDirectiveEditor({
  mdastNode,
}: DirectiveEditorProps<LeafDirective>) {
  const [editing, setEditing] = useState(false);
  const updateNode = useMdastNodeUpdater<LeafDirective>();
  const removeNode = useLexicalNodeRemove();
  const videoId = mdastNode.attributes?.videoId ?? '';
  const title = mdastNode.attributes?.title?.trim() ?? '';
  const valid = isValidYouTubeVideoId(videoId) && Boolean(title);

  function update(values: YouTubeValues) {
    updateNode({
      attributes: { videoId: values.videoId, title: values.title },
    });
    setEditing(false);
  }

  return (
    <div
      className={`${styles.youtubeEditor} ${valid ? '' : styles.youtubeInvalid}`}
      contentEditable={false}
    >
      <div className={styles.youtubeEditorIcon} aria-hidden='true'>
        <YoutubeLogoIcon size={28} weight='fill' />
      </div>
      <div className={styles.youtubeEditorInfo}>
        <span>Vídeo do YouTube</span>
        <strong>{valid ? title : 'Diretiva de vídeo inválida'}</strong>
        {valid && (
          <a
            href={getYouTubeWatchUrl(videoId)}
            target='_blank'
            rel='noopener noreferrer'
          >
            Visualizar vídeo
          </a>
        )}
      </div>
      <div className={styles.youtubeEditorActions}>
        <button
          type='button'
          onClick={() => setEditing(true)}
          aria-label={`Editar vídeo${title ? `: ${title}` : ''}`}
        >
          <PencilSimpleIcon size={18} />
        </button>
        <button
          type='button'
          onClick={removeNode}
          aria-label={`Excluir vídeo${title ? `: ${title}` : ''}`}
        >
          <TrashIcon size={18} />
        </button>
      </div>
      {editing && (
        <YouTubeDialog
          editing
          initial={{ videoId, title }}
          save={update}
          close={() => setEditing(false)}
        />
      )}
    </div>
  );
}
