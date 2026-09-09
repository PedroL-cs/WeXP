import styles from './styles.module.css';

type DiffType = 'added' | 'changed' | 'removed' | 'unchanged';
type DiffLine = { value: string; type: DiffType; prefix: '+' | '−' | ' ' };

function toLines(content: string) {
  const lines = content.replace(/\r\n?/g, '\n').split('\n');

  // A quebra de linha final não altera o Markdown, mas o split a transforma
  // em uma linha vazia visível no comparador.
  while (lines.length > 1 && lines.at(-1)?.trim() === '') {
    lines.pop();
  }

  // No Markdown, uma ou várias linhas vazias têm o mesmo efeito: separar
  // blocos. O MDXEditor pode serializar mais de uma delas ao editar um
  // parágrafo, portanto preservamos somente uma para não poluir o diff.
  return lines.filter(
    (line, index) => line.trim() !== '' || index === 0 || lines[index - 1].trim() !== '',
  );
}

function getDiff(previous: string, current: string): DiffLine[] {
  const before = toLines(previous);
  const after = toLines(current);
  const matrix = Array.from({ length: before.length + 1 }, () =>
    Array<number>(after.length + 1).fill(0),
  );

  for (let i = before.length - 1; i >= 0; i -= 1) {
    for (let j = after.length - 1; j >= 0; j -= 1) {
      matrix[i][j] =
        before[i] === after[j]
          ? matrix[i + 1][j + 1] + 1
          : Math.max(matrix[i + 1][j], matrix[i][j + 1]);
    }
  }

  const result: DiffLine[] = [];
  let i = 0;
  let j = 0;

  while (i < before.length && j < after.length) {
    if (before[i] === after[j]) {
      result.push({ value: before[i], type: 'unchanged', prefix: ' ' });
      i += 1;
      j += 1;
    } else if (matrix[i + 1][j] >= matrix[i][j + 1]) {
      result.push({ value: before[i], type: 'removed', prefix: '−' });
      i += 1;
    } else {
      result.push({ value: after[j], type: 'added', prefix: '+' });
      j += 1;
    }
  }

  while (i < before.length) {
    result.push({ value: before[i], type: 'removed', prefix: '−' });
    i += 1;
  }
  while (j < after.length) {
    result.push({ value: after[j], type: 'added', prefix: '+' });
    j += 1;
  }

  return result.map((line, index, lines): DiffLine => {
    const previousLine = lines[index - 1];
    const nextLine = lines[index + 1];

    if (
      (line.type === 'removed' && nextLine?.type === 'added') ||
      (line.type === 'added' && previousLine?.type === 'removed')
    ) {
      return { ...line, type: 'changed' };
    }

    return line;
  }).filter(line => line.type !== 'unchanged' || line.value.trim() !== '');
}

function GuideDiff({ previous, current }: { previous: string; current: string }) {
  return (
    <div className={styles.diff} aria-label='Diferenças entre as versões do guia'>
      {getDiff(previous, current).map((line, index) => (
        <div
          className={`${styles.line} ${styles[line.type]}`}
          key={`${line.type}-${index}-${line.value}`}
        >
          <span aria-hidden='true'>{line.prefix}</span>
          <span>{line.value || ' '}</span>
        </div>
      ))}
    </div>
  );
}

export default GuideDiff;
