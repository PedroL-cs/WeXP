import ReactMarkdown from 'react-markdown';
import remarkBreaks from 'remark-breaks';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';

import styles from './styles.module.css';

type GuideMarkdownProps = {
  markdown: string;
};

function GuideMarkdown({ markdown }: GuideMarkdownProps) {
  return (
    <div className={styles.guideMarkdown}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm, remarkBreaks]}
        rehypePlugins={[rehypeRaw]}
      >
        {markdown}
      </ReactMarkdown>
    </div>
  );
}

export default GuideMarkdown;
