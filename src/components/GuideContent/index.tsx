import { memo } from 'react';
import styles from './styles.module.css';
import MarkdownRenderer from '../MarkdownRenderer';

type GuideContentProps = {
  markdown: string;
};

function GuideContent({ markdown }: GuideContentProps) {
  return (
    <div className={styles.guideContent}>
      <MarkdownRenderer markdown={markdown} />
    </div>
  );
}

export default memo(GuideContent);
