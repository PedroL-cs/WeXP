import {
  MDXEditor,
  headingsPlugin,
  listsPlugin,
  quotePlugin,
  markdownShortcutPlugin,
  thematicBreakPlugin,
  linkPlugin,
  toolbarPlugin,
  UndoRedo,
  BoldItalicUnderlineToggles,
  BlockTypeSelect,
  ListsToggle,
  CreateLink,
  linkDialogPlugin,
  CodeToggle,
  imagePlugin,
  InsertImage,
  ButtonWithTooltip,
  directivesPlugin,
  type MDXEditorMethods,
  type Translation,
} from '@mdxeditor/editor';
import { YoutubeLogoIcon } from '@phosphor-icons/react';
import { useRef, useState, type ClipboardEvent } from 'react';
import '@mdxeditor/editor/style.css';
import styles from './styles.module.css';
import ImageUrlDialog from './ImageUrlDialog';
import { imageMarkdownFromPaste } from './imageMarkdown';
import { guideImagePolicyPlugin, previewGuideImage } from './imagePolicyPlugin';
import YouTubeDialog from './YouTubeDialog';
import { youtubeDirectiveDescriptor } from './youtubeDirective';
import {
  youtubeMarkdown,
  youtubeVideoIdFromPaste,
} from './youtubeMarkdown';

const labels: Record<string, string> = {
  'toolbar.image': 'Inserir imagem por URL',
  'toolbar.source': 'Markdown',
  'toolbar.richText': 'Editor visual',
  'imageEditor.editImage': 'Editar imagem',
  'imageEditor.deleteImage': 'Excluir imagem',
};

type GuideEditorProps = {
  initialMarkdown?: string;
  onChange?: (markdown: string) => void;
};

const translate: Translation = (key, fallback, interpolations = {}) => {
  const template = labels[key] ?? fallback;
  return template.replace(/\{\{(\w+)\}\}/g, (placeholder, name) =>
    name in interpolations ? String(interpolations[name]) : placeholder,
  );
};

function InsertYouTubeButton({ open }: { open: () => void }) {
  return (
    <ButtonWithTooltip
      title='Inserir vídeo do YouTube'
      type='button'
      onClick={open}
    >
      <YoutubeLogoIcon size={18} />
    </ButtonWithTooltip>
  );
}

function GuideEditor({
  initialMarkdown = '# Comece a digitar',
  onChange,
}: GuideEditorProps) {
  const editor = useRef<MDXEditorMethods>(null);
  const [mode, setMode] = useState<'visual' | 'markdown'>('visual');
  const [markdown, setMarkdown] = useState(initialMarkdown);
  const [youtubeDialogUrl, setYoutubeDialogUrl] = useState<string | null>(null);

  function updateMarkdown(value: string) {
    setMarkdown(value);
    onChange?.(value);
  }
  function pasteImages(event: ClipboardEvent<HTMLDivElement>) {
    if (
      !(event.target instanceof HTMLElement) ||
      !event.target.closest('.mdxeditor-root-contenteditable')
    )
      return;
    const pasted = event.clipboardData.getData('text/plain');
    const imageMarkdown = imageMarkdownFromPaste(pasted);
    if (imageMarkdown && editor.current) {
      event.preventDefault();
      event.stopPropagation();
      editor.current.insertMarkdown(imageMarkdown);
      return;
    }
    if (youtubeVideoIdFromPaste(pasted)) {
      event.preventDefault();
      event.stopPropagation();
      setYoutubeDialogUrl(pasted.trim());
    }
  }
  return (
    <div className={styles.editor} onPasteCapture={pasteImages}>
      <p className={styles.help}>
        Para imagens, use “Inserir imagem por URL”, cole a sintaxe{' '}
        <code>![descrição](https://...)</code>. Para vídeos, use o botão do
        YouTube ou cole a URL de um vídeo no editor visual.
      </p>
      <div
        className={styles.modeSwitch}
        role='group'
        aria-label='Modo de edição'
      >
        <button
          type='button'
          aria-pressed={mode === 'visual'}
          onClick={() => setMode('visual')}
        >
          Editor visual
        </button>
        <button
          type='button'
          aria-pressed={mode === 'markdown'}
          onClick={() => setMode('markdown')}
        >
          Markdown
        </button>
      </div>
      {mode === 'markdown' ? (
        <textarea
          className={styles.markdownSource}
          aria-label='Conteúdo em Markdown'
          value={markdown}
          spellCheck={false}
          onChange={event => updateMarkdown(event.target.value)}
        />
      ) : (
        <MDXEditor
          ref={editor}
          markdown={markdown}
          onChange={updateMarkdown}
          translation={translate}
          plugins={[
            headingsPlugin(),
            listsPlugin(),
            quotePlugin(),
            thematicBreakPlugin(),
            linkPlugin(),
            linkDialogPlugin(),
            imagePlugin({
              ImageDialog: ImageUrlDialog,
              imagePreviewHandler: previewGuideImage,
              disableImageResize: true,
              allowSetImageDimensions: false,
            }),
            guideImagePolicyPlugin(),
            directivesPlugin({
              directiveDescriptors: [youtubeDirectiveDescriptor],
              escapeUnknownTextDirectives: true,
            }),
            markdownShortcutPlugin(),
            toolbarPlugin({
              toolbarClassName: styles.toolbar,
              toolbarContents: () => (
                <>
                  <UndoRedo />
                  <BoldItalicUnderlineToggles />
                  <BlockTypeSelect />
                  <ListsToggle options={['bullet', 'number']} />
                  <CodeToggle />
                  <CreateLink />
                  <InsertImage />
                  <InsertYouTubeButton
                    open={() => setYoutubeDialogUrl('')}
                  />
                </>
              ),
            }),
          ]}
        />
      )}
      {youtubeDialogUrl !== null && (
        <YouTubeDialog
          initial={{ url: youtubeDialogUrl }}
          save={values => {
            editor.current?.insertMarkdown(
              youtubeMarkdown(values.videoId, values.title),
            );
            setYoutubeDialogUrl(null);
          }}
          close={() => setYoutubeDialogUrl(null)}
        />
      )}
    </div>
  );
}

export default GuideEditor;
