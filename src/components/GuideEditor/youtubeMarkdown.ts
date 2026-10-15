import { directiveToMarkdown, type LeafDirective } from 'mdast-util-directive';
import { toMarkdown } from 'mdast-util-to-markdown';
import { extractYouTubeVideoId } from '../MarkdownRenderer/youtubePolicy';

export function youtubeMarkdown(videoId: string, title: string) {
  const directive: LeafDirective = {
    type: 'leafDirective',
    name: 'youtube',
    attributes: { videoId, title: title.trim() },
    children: [],
  };

  return toMarkdown(
    { type: 'root', children: [directive] },
    { extensions: [directiveToMarkdown()] },
  );
}

export function youtubeVideoIdFromPaste(value: string) {
  return extractYouTubeVideoId(value.trim());
}
