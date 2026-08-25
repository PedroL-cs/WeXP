import { useEffect } from 'react';

import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import { $convertFromMarkdownString } from '@lexical/markdown';
import { TRANSFORMERS } from '@lexical/markdown';

interface MarkdownPluginProps {
  markdown?: string;
}

export function MarkdownPlugin({ markdown }: MarkdownPluginProps) {
  const [editor] = useLexicalComposerContext();

  useEffect(() => {
    if (!markdown) {
      return;
    }

    editor.update(() => {
      $convertFromMarkdownString(markdown, TRANSFORMERS);
    });
  }, [editor, markdown]);

  return null;
}
