import styles from './styles.module.css';

type DiffLine = {
  value: string;
  type: 'added' | 'removed' | 'unchanged';
};

type GuideDiffProps = {
  previous: string;
  current: string;
};

function getDiff(previous: string, current: string): DiffLine[] {
  const before = previous.split('\n');
  const after = current.split('\n');
  const matrix = Array.from({ length: before.length + 1 }, () =>
    Array<number>(after.length + 1).fill(0),
  );

  for (let beforeIndex = before.length - 1; beforeIndex >= 0; beforeIndex -= 1) {
    for (let afterIndex = after.length - 1; afterIndex >= 0; afterIndex -= 1) {
      matrix[beforeIndex][afterIndex] =
        before[beforeIndex] === after[afterIndex]
          ? matrix[beforeIndex + 1][afterIndex + 1] + 1
          : Math.max(matrix[beforeIndex + 1][afterIndex], matrix[beforeIndex][afterIndex + 1]);
    }
  }

  const result: DiffLine[] = [];
  let beforeIndex = 0;
  let afterIndex = 0;

  while (beforeIndex < before.length && afterIndex < after.length) {
    if (before[beforeIndex] === after[afterIndex]) {
      result.push({ value: before[beforeIndex], type: 'unchanged' });
      beforeIndex += 1;
      afterIndex += 1;
    } else if (matrix[beforeIndex + 1][afterIndex] >= matrix[beforeIndex][afterIndex + 1]) {
      result.push({ value: before[beforeIndex], type: 'removed' });
      beforeIndex += 1;
    } else {
      result.push({ value: after[afterIndex], type: 'added' });
      afterIndex += 1;
    }
  }

  while (beforeIndex < before.length) {
    result.push({ value: before[beforeIndex], type: 'removed' });
    beforeIndex += 1;
  }

  while (afterIndex < after.length) {
    result.push({ value: after[afterIndex], type: 'added' });
    afterIndex += 1;
  }

  return result;
}

function GuideDiff({ previous, current }: GuideDiffProps) {
  const diff = getDiff(previous, current);

  return (
    <div className={styles.diff} aria-label='Diferenças entre as versões do guia'>
      {diff.map((line, index) => (
        <div className={`${styles.line} ${styles[line.type]}`} key={`${line.type}-${index}-${line.value}`}>
          <span className={styles.marker} aria-hidden='true'>
            {line.type === 'added' ? '+' : line.type === 'removed' ? '−' : ' '}
          </span>
          <span>{line.value || ' '}</span>
        </div>
      ))}
    </div>
  );
}

export default GuideDiff;
