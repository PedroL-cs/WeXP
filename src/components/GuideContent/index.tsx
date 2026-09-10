import ReactMarkdown from 'react-markdown';
import styles from './styles.module.css';
import remarkGfm from 'remark-gfm';
import remarkBreaks from 'remark-breaks';
import rehypeRaw from 'rehype-raw';

type GuideContentProps = {
  markdown: string;
};

function GuideContent({ markdown }: GuideContentProps) {
  return (
    <div className={styles.guideContent}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm, remarkBreaks]}
        rehypePlugins={[rehypeRaw]}
      >
        {markdown}
      </ReactMarkdown>
    </div>
  );
}

export default GuideContent;
