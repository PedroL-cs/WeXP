import type { Root, RootContent } from 'mdast';
import type { LeafDirective } from 'mdast-util-directive';
import { isValidYouTubeVideoId } from './youtubePolicy';

function visit(node: Root | RootContent) {
  if (node.type === 'leafDirective' && node.name === 'youtube') {
    const directive = node as LeafDirective;
    const videoId = directive.attributes?.videoId;
    const title = directive.attributes?.title?.trim();

    directive.data = {
      ...directive.data,
      hName: 'youtube-embed',
      hProperties: {
        dataVideoId: isValidYouTubeVideoId(videoId) ? videoId : '',
        dataTitle: title || '',
      },
    };
  }

  if ('children' in node) node.children.forEach(child => visit(child));
}

export default function remarkYoutube() {
  return (tree: Root) => visit(tree);
}
