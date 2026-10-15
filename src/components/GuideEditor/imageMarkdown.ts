import { fromMarkdown } from 'mdast-util-from-markdown';
import { toMarkdown } from 'mdast-util-to-markdown';
import type { Image, Paragraph } from 'mdast';
import { isAllowedImageUrl } from '../MarkdownRenderer/markdownPolicy';

export function normalizeImageUrl(value: string) {
  // Markdown copied from formatted text can escape underscores in URL paths.
  return value.trim().replace(/\\_/g, '_');
}

export function imageMarkdownFromPaste(value: string): string | null {
  const root = fromMarkdown(value);
  const definitions = new Map(
    root.children
      .filter(node => node.type === 'definition')
      .map(node => [node.identifier.toLowerCase(), node]),
  );
  const images: Image[] = [];

  for (const block of root.children) {
    if (block.type === 'definition') continue;
    if (block.type !== 'paragraph') return null;
    for (const node of block.children) {
      if (node.type === 'break' || (node.type === 'text' && !node.value.trim()))
        continue;
      let image: Image;
      if (node.type === 'image') image = node;
      else if (node.type === 'imageReference') {
        const definition = definitions.get(node.identifier.toLowerCase());
        if (!definition) return null;
        image = {
          type: 'image',
          url: definition.url,
          alt: node.alt,
          title: definition.title,
        };
      } else return null;
      const url = normalizeImageUrl(image.url);
      if (!isAllowedImageUrl(url)) return null;
      images.push({ type: 'image', url, alt: image.alt, title: image.title });
    }
  }
  if (!images.length) return null;
  const children: Paragraph[] = images.map(image => ({
    type: 'paragraph',
    children: [image],
  }));
  return toMarkdown({ type: 'root', children });
}
