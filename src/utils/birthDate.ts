export function birthDateDigits(value: string) {
  return value.replace(/\D/g, '');
}

export function formatBirthDate(value: string) {
  const digits = birthDateDigits(value).slice(0, 8);
  return [digits.slice(0, 2), digits.slice(2, 4), digits.slice(4, 8)]
    .filter(Boolean)
    .join('/');
}

export function birthDateCursor(value: string, digitOffset: number) {
  if (digitOffset <= 0) return 0;
  let digits = 0;
  for (let index = 0; index < value.length; index += 1) {
    if (value[index] !== '/') digits += 1;
    if (digits === digitOffset) {
      return index + 1 + (value[index + 1] === '/' ? 1 : 0);
    }
  }
  return value.length;
}

export function maskBirthDateChange(
  previous: string,
  input: string,
  cursor: number,
  inputType?: string,
) {
  let digits = birthDateDigits(input);
  let digitOffset = birthDateDigits(input.slice(0, cursor)).length;

  // Deleting only a separator should remove the adjacent digit instead of
  // reinserting the slash and trapping the caret, including on mobile keyboards.
  if (input.length < previous.length && digits === birthDateDigits(previous)) {
    if (inputType === 'deleteContentBackward' && digitOffset > 0) {
      digits = digits.slice(0, digitOffset - 1) + digits.slice(digitOffset);
      digitOffset -= 1;
    } else if (inputType === 'deleteContentForward') {
      digits = digits.slice(0, digitOffset) + digits.slice(digitOffset + 1);
    }
  }

  const value = formatBirthDate(digits);
  return { value, cursor: birthDateCursor(value, digitOffset) };
}

export function getBirthDateError(value: string, today = new Date()) {
  if (!value) return 'Informe sua data de nascimento.';
  if (!/^\d{2}\/\d{2}\/\d{4}$/.test(value)) {
    return 'Informe a data completa no formato dd/mm/aaaa.';
  }

  const [day, month, year] = value.split('/').map(Number);
  if (year < 1 || month < 1 || month > 12 || day < 1 || day > 31) {
    return 'Informe uma data de nascimento válida.';
  }

  // UTC components validate the calendar without timezone/DST normalization.
  // setUTCFullYear also avoids treating years 0001–0099 as 1901–1999.
  const date = new Date(0);
  date.setUTCFullYear(year, month - 1, day);
  if (
    date.getUTCFullYear() !== year ||
    date.getUTCMonth() !== month - 1 ||
    date.getUTCDate() !== day
  ) {
    return 'Informe uma data de nascimento válida.';
  }

  const birthKey = year * 10000 + month * 100 + day;
  const todayKey =
    today.getFullYear() * 10000 +
    (today.getMonth() + 1) * 100 +
    today.getDate();
  if (birthKey >= todayKey) {
    return 'A data de nascimento deve ser anterior à data de hoje.';
  }
  return '';
}
