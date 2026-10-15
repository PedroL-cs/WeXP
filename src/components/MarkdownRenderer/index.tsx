import { memo } from 'react';
import ReactMarkdown, { type Components } from 'react-markdown';
import remarkBreaks from 'remark-breaks';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import rehypeSanitize from 'rehype-sanitize';
import type { PluggableList } from 'unified';
import ExternalImage from './ExternalImage';
import { guideMarkdownSchema, remarkRestrictGuideHtml } from './markdownPolicy';

const remarkPlugins: PluggableList = [
  remarkGfm,
  remarkBreaks,
  remarkRestrictGuideHtml,
];
const rehypePlugins: PluggableList = [
  rehypeRaw,
  [rehypeSanitize, guideMarkdownSchema],
];

const components: Components = {
  img({ src, alt, title }) {
    const imageUrl = typeof src === 'string' ? src : undefined;
    // Changing the source remounts the image so an earlier error is not retained.
    return (
      <ExternalImage
        key={imageUrl ?? 'invalid-image'}
        src={imageUrl}
        alt={alt}
        title={title}
      />
    );
  },
};

function MarkdownRenderer({ markdown }: { markdown: string }) {
  return (
    <ReactMarkdown
      remarkPlugins={remarkPlugins}
      rehypePlugins={rehypePlugins}
      components={components}
    >
      {markdown}
    </ReactMarkdown>
  );
}

export default memo(MarkdownRenderer);
