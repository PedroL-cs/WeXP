import ReactMarkdown from 'react-markdown';
import styles from './styles.module.css';
import remarkGfm from 'remark-gfm';
import remarkBreaks from 'remark-breaks';

type GuideContentProps = {
  markdown: string;
};

function GuideContent({ markdown }: GuideContentProps) {
  return (
    <div className={styles.guideContent}>
      <ReactMarkdown remarkPlugins={[remarkGfm, remarkBreaks]}>
        {markdown}
      </ReactMarkdown>
    </div>
  );
}

export default GuideContent;
