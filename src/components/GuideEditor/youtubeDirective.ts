import type { DirectiveDescriptor } from '@mdxeditor/editor';
import type { LeafDirective } from 'mdast-util-directive';
import YouTubeDirectiveEditor from './YouTubeDirectiveEditor';

export const youtubeDirectiveDescriptor: DirectiveDescriptor<LeafDirective> = {
  name: 'youtube',
  type: 'leafDirective',
  attributes: ['videoId', 'title'],
  hasChildren: false,
  testNode: node => node.type === 'leafDirective' && node.name === 'youtube',
  Editor: YouTubeDirectiveEditor,
};
