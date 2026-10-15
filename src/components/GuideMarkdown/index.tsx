import { memo } from 'react';
import MarkdownRenderer from '../MarkdownRenderer';

import styles from './styles.module.css';

type GuideMarkdownProps = {
  markdown: string;
};

function GuideMarkdown({ markdown }: GuideMarkdownProps) {
  return (
    <div className={styles.guideMarkdown}>
      <MarkdownRenderer markdown={markdown} />
    </div>
  );
}

export default memo(GuideMarkdown);
