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
} from '@mdxeditor/editor';
import '@mdxeditor/editor/style.css';
import styles from './styles.module.css';

type GuideEditorProps = {
  initialMarkdown?: string;
  onChange?: (markdown: string) => void;
};

function GuideEditor({
  initialMarkdown = '# Comece a digitar',
  onChange,
}: GuideEditorProps) {
  return (
    <div className={styles.editor}>
      <MDXEditor
        markdown={initialMarkdown}
        onChange={onChange}
        plugins={[
          headingsPlugin(),
          listsPlugin(),
          quotePlugin(),
          thematicBreakPlugin(),
          linkPlugin(),
          linkDialogPlugin(),
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
              </>
            ),
          }),
        ]}
      />
    </div>
  );
}

export default GuideEditor;
