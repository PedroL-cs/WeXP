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
  type MDXEditorMethods,
} from '@mdxeditor/editor';
import { useRef, useState, type ClipboardEvent } from 'react';
import '@mdxeditor/editor/style.css';
import styles from './styles.module.css';
import ImageUrlDialog from './ImageUrlDialog';
import { imageMarkdownFromPaste } from './imageMarkdown';
import { guideImagePolicyPlugin, previewGuideImage } from './imagePolicyPlugin';

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

function GuideEditor({
  initialMarkdown = '# Comece a digitar',
  onChange,
}: GuideEditorProps) {
  const editor = useRef<MDXEditorMethods>(null);
  const [mode, setMode] = useState<'visual' | 'markdown'>('visual');
  const [markdown, setMarkdown] = useState(initialMarkdown);

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
    const markdown = imageMarkdownFromPaste(
      event.clipboardData.getData('text/plain'),
    );
    if (!markdown || !editor.current) return;
    event.preventDefault();
    event.stopPropagation();
    editor.current.insertMarkdown(markdown);
  }
  return (
    <div className={styles.editor} onPasteCapture={pasteImages}>
      <p className={styles.help}>
        Para imagens, use “Inserir imagem por URL”, cole a sintaxe{' '}
        <code>![descrição](https://...)</code> ou escreva no modo Markdown.
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
          translation={(key, fallback) => labels[key] ?? fallback}
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
                </>
              ),
            }),
          ]}
        />
      )}
    </div>
  );
}

export default GuideEditor;
