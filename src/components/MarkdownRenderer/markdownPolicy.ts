import type { Nodes, Root } from 'mdast';
import { defaultSchema, type Options } from 'rehype-sanitize';

export function isAllowedImageUrl(value: unknown): value is string {
  if (
    typeof value !== 'string' ||
    !/^https:\/\//i.test(value) ||
    /\s|\\/.test(value) ||
    Array.from(value).some(
      character =>
        character.charCodeAt(0) < 32 || character.charCodeAt(0) === 127,
    )
  ) {
    return false;
  }

  try {
    const url = new URL(value);
    return (
      url.protocol === 'https:' &&
      Boolean(url.hostname) &&
      !url.username &&
      !url.password
    );
  } catch {
    return false;
  }
}

export function isAllowedGuideHtml(value: string) {
  return /^<\/?u\s*>$/i.test(value);
}

function restrictHtml(node: Nodes) {
  // MDXEditor uses bare <u> tags for underline. All other user-written HTML,
  // including <img> and <iframe>, is removed before it can become DOM elements.
  if (node.type === 'html' && !isAllowedGuideHtml(node.value)) {
    node.value = '';
  }
  if ('children' in node) node.children.forEach(restrictHtml);
}

export function remarkRestrictGuideHtml() {
  return (tree: Root) => restrictHtml(tree);
}

export const guideMarkdownSchema: Options = {
  ...defaultSchema,
  tagNames: [...(defaultSchema.tagNames ?? []), 'u', 'youtube-embed'],
  attributes: {
    ...defaultSchema.attributes,
    img: ['src', 'alt', 'title'],
    u: [],
    'youtube-embed': ['dataVideoId', 'dataTitle'],
  },
  protocols: {
    ...defaultSchema.protocols,
    src: ['https'],
  },
  strip: [
    ...(defaultSchema.strip ?? []),
    'iframe',
    'style',
    'object',
    'embed',
    'svg',
    'math',
  ],
};
