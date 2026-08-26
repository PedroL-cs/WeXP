import {
  MDXEditor,
  headingsPlugin,
  listsPlugin,
  quotePlugin,
  markdownShortcutPlugin,
  thematicBreakPlugin,
  linkPlugin,
  codeBlockPlugin,
  toolbarPlugin,
  UndoRedo,
  BoldItalicUnderlineToggles,
  BlockTypeSelect,
  ListsToggle,
  CreateLink,
} from '@mdxeditor/editor';

import '@mdxeditor/editor/style.css';
import styles from './styles.module.css';

function GuideEditor() {
  const initialMarkdown = `# Comece a digitar

Experimente usar **negrito**, *itálico*, # ou > para formatar na hora.
`;
  return (
    <div className={styles.editor}>
      <MDXEditor
        markdown={initialMarkdown}
        onChange={markdown => {
          console.log(markdown);
        }}
        plugins={[
          headingsPlugin(),
          listsPlugin(),
          quotePlugin(),
          thematicBreakPlugin(),
          linkPlugin(),
          codeBlockPlugin(),
          markdownShortcutPlugin(),

          toolbarPlugin({
            toolbarClassName: styles.toolbar,
            toolbarContents: () => (
              <>
                <UndoRedo />
                <BoldItalicUnderlineToggles />
                <BlockTypeSelect />
                <ListsToggle options={['bullet', 'number']} />
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
