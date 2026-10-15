import { memo, type ImgHTMLAttributes } from 'react';
import ReactMarkdown, { type Components } from 'react-markdown';
import remarkBreaks from 'remark-breaks';
import remarkGfm from 'remark-gfm';
import remarkDirective from 'remark-directive';
import rehypeRaw from 'rehype-raw';
import rehypeSanitize from 'rehype-sanitize';
import type { PluggableList } from 'unified';
import ExternalImage from './ExternalImage';
import { guideMarkdownSchema, remarkRestrictGuideHtml } from './markdownPolicy';
import remarkYoutube from './remarkYoutube';
import YouTubeEmbed from './YouTubeEmbed';

const remarkPlugins: PluggableList = [
  remarkGfm,
  remarkBreaks,
  remarkDirective,
  remarkYoutube,
  remarkRestrictGuideHtml,
];
const rehypePlugins: PluggableList = [
  rehypeRaw,
  [rehypeSanitize, guideMarkdownSchema],
];

const components = {
  img({ src, alt, title }: ImgHTMLAttributes<HTMLImageElement>) {
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
  'youtube-embed'({
    'data-video-id': videoId,
    'data-title': title,
  }: {
    'data-video-id'?: string;
    'data-title'?: string;
  }) {
    return <YouTubeEmbed videoId={videoId} title={title} />;
  },
} as unknown as Components;

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
