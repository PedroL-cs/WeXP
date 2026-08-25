import { LexicalComposer } from '@lexical/react/LexicalComposer';
import { ContentEditable } from '@lexical/react/LexicalContentEditable';
import { LexicalErrorBoundary } from '@lexical/react/LexicalErrorBoundary';
import { HistoryPlugin } from '@lexical/react/LexicalHistoryPlugin';
import { RichTextPlugin } from '@lexical/react/LexicalRichTextPlugin';
import { MarkdownShortcutPlugin } from '@lexical/react/LexicalMarkdownShortcutPlugin';
import { OnChangePlugin } from '@lexical/react/LexicalOnChangePlugin';

import { HeadingNode, QuoteNode } from '@lexical/rich-text';
import { ListNode, ListItemNode } from '@lexical/list';
import { CodeNode, CodeHighlightNode } from '@lexical/code';
import { AutoLinkNode, LinkNode } from '@lexical/link';
import { HorizontalRuleNode } from '@lexical/react/LexicalHorizontalRuleNode';

import { $convertToMarkdownString, TRANSFORMERS } from '@lexical/markdown';

import styles from './styles.module.css';
import { MarkdownPlugin } from './markdownPlugin';
import { ListPlugin } from '@lexical/react/LexicalListPlugin';

const initialConfig = {
  namespace: 'GuideEditor',
  nodes: [
    HeadingNode,
    QuoteNode,
    ListNode,
    ListItemNode,
    CodeNode,
    CodeHighlightNode,
    AutoLinkNode,
    LinkNode,
    HorizontalRuleNode,
  ],
  onError(error: Error) {
    throw error;
  },
};

type guideEditorProps = {
  initialMarkdown?: string;
  onChange?: (markdown: string) => void;
};

function GuideEditor({ initialMarkdown, onChange }: guideEditorProps) {
  return (
    <LexicalComposer initialConfig={initialConfig}>
      <div className={styles.editor}>
        <RichTextPlugin
          contentEditable={
            <ContentEditable className={styles.contentEditable} />
          }
          placeholder={
            <div className={styles.placeholder}>Escreva seu guia...</div>
          }
          ErrorBoundary={LexicalErrorBoundary}
        />

        <HistoryPlugin />

        <ListPlugin />

        <MarkdownShortcutPlugin transformers={TRANSFORMERS} />

        <MarkdownPlugin markdown={initialMarkdown} />

        <OnChangePlugin
          onChange={editorState => {
            editorState.read(() => {
              const markdown = $convertToMarkdownString(TRANSFORMERS);

              onChange?.(markdown);
            });
          }}
        />
      </div>
    </LexicalComposer>
  );
}

export default GuideEditor;
