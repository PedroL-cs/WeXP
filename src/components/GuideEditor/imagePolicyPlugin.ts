import {
  $isImageNode,
  addExportVisitor$,
  addImportVisitor$,
  realmPlugin,
  type ImageNode,
  type LexicalExportVisitor,
  type MdastImportVisitor,
} from '@mdxeditor/editor';
import type { Html, Image } from 'mdast';
import {
  isAllowedGuideHtml,
  isAllowedImageUrl,
} from '../MarkdownRenderer/markdownPolicy';
import { normalizeImageUrl } from './imageMarkdown';

const blockUserHtml: MdastImportVisitor<Html> = {
  priority: 100,
  testNode: node => node.type === 'html' && !isAllowedGuideHtml(node.value),
  visitNode() {
    /* Match the reader's HTML policy before imagePlugin imports HTML. */
  },
};

const exportMarkdownImages: LexicalExportVisitor<ImageNode, Image> = {
  priority: 100,
  testLexicalNode: $isImageNode,
  visitLexicalNode({ lexicalNode, mdastParent, actions }) {
    actions.appendToParent(mdastParent, {
      type: 'image',
      url: normalizeImageUrl(lexicalNode.getSrc()),
      alt: lexicalNode.getAltText(),
      title: lexicalNode.getTitle(),
    });
  },
};

export const guideImagePolicyPlugin = realmPlugin({
  init(realm) {
    realm.pub(addImportVisitor$, blockUserHtml);
    realm.pub(addExportVisitor$, exportMarkdownImages);
  },
});

export async function previewGuideImage(source: string) {
  const url = normalizeImageUrl(source);
  return isAllowedImageUrl(url) ? url : '/guide-image-unavailable.svg';
}
