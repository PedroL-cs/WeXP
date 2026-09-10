import React from 'react';
import GuideMarkdown from '../GuideMarkdown';
import styles from './styles.module.css';

type GuideDiffProps = {
  previous: string;
  current: string;
};

function GuideDiff({ previous, current }: GuideDiffProps) {
  const blocks = getBlockDiff(previous, current);

  console.log('blocks', blocks);

  return (
    <div className={styles.diff}>
      <div className={styles.panel}>
        <header className={styles.panelHeader}>
          <span>Versão anterior</span>
        </header>

        {blocks.map((block, index) => (
          <React.Fragment key={`previous-${index}`}>
            {block.previous &&
              (block.type === 'removed' || block.type === 'changed' ? (
                <div className={`${styles.block} ${styles[block.type]}`}>
                  <GuideMarkdown markdown={block.previous} />
                </div>
              ) : (
                <GuideMarkdown markdown={block.previous} />
              ))}
          </React.Fragment>
        ))}
      </div>

      <div className={styles.panel}>
        <header className={styles.panelHeader}>
          <span>Versão atual</span>
        </header>

        {blocks.map((block, index) => (
          <React.Fragment key={`current-${index}`}>
            {block.current &&
              (block.type === 'added' || block.type === 'changed' ? (
                <div className={`${styles.block} ${styles[block.type]}`}>
                  <GuideMarkdown markdown={block.current} />
                </div>
              ) : (
                <GuideMarkdown markdown={block.current} />
              ))}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
}

type DiffType = 'unchanged' | 'added' | 'removed' | 'changed';

type DiffBlock = {
  previous: string | null;
  current: string | null;
  type: DiffType;
};

function toBlocks(content: string): string[] {
  return content
    .replace(/\r\n?/g, '\n')
    .split(/\n{2,}/)
    .map(block => block.trim())
    .filter(Boolean);
}

function getBlockDiff(previous: string, current: string): DiffBlock[] {
  const before = toBlocks(previous);
  const after = toBlocks(current);

  const matrix = Array.from({ length: before.length + 1 }, () =>
    Array<number>(after.length + 1).fill(0),
  );

  // LCS entre os blocos
  for (let i = before.length - 1; i >= 0; i -= 1) {
    for (let j = after.length - 1; j >= 0; j -= 1) {
      matrix[i][j] =
        before[i] === after[j]
          ? matrix[i + 1][j + 1] + 1
          : Math.max(matrix[i + 1][j], matrix[i][j + 1]);
    }
  }

  const result: DiffBlock[] = [];

  let i = 0;
  let j = 0;

  while (i < before.length && j < after.length) {
    if (before[i] === after[j]) {
      result.push({
        previous: before[i],
        current: after[j],
        type: 'unchanged',
      });

      i += 1;
      j += 1;
    } else if (matrix[i + 1][j] >= matrix[i][j + 1]) {
      result.push({
        previous: before[i],
        current: null,
        type: 'removed',
      });

      i += 1;
    } else {
      result.push({
        previous: null,
        current: after[j],
        type: 'added',
      });

      j += 1;
    }
  }

  while (i < before.length) {
    result.push({
      previous: before[i],
      current: null,
      type: 'removed',
    });

    i += 1;
  }

  while (j < after.length) {
    result.push({
      previous: null,
      current: after[j],
      type: 'added',
    });

    j += 1;
  }

  // Se um bloco removido é imediatamente seguido
  // por um bloco adicionado, consideramos que o bloco foi alterado.
  const diff: DiffBlock[] = [];

  for (let index = 0; index < result.length; index += 1) {
    const block = result[index];
    const next = result[index + 1];

    if (block.type === 'removed' && next?.type === 'added') {
      diff.push({
        previous: block.previous,
        current: next.current,
        type: 'changed',
      });

      index += 1;
      continue;
    }

    diff.push(block);
  }

  return diff;
}

export default GuideDiff;
