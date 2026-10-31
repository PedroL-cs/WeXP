export class ApiError extends Error {
  status: number;
  code?: number;
  fieldErrors: Record<string, string>;

  constructor(
    message: string,
    status: number,
    code?: number,
    fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
  }
}

export async function throwApiError(
  response: Response,
  fallback: string,
): Promise<never> {
  const error = await response.json().catch(() => null);
  const fields = new Map<string, Set<string>>();
  if (Array.isArray(error?.errors)) {
    for (const entry of error.errors) {
      if (
        typeof entry?.field !== 'string' ||
        typeof entry?.message !== 'string' ||
        !entry.message.trim()
      ) {
        continue;
      }
      const messages = fields.get(entry.field) ?? new Set<string>();
      messages.add(entry.message.trim());
      fields.set(entry.field, messages);
    }
  }
  throw new ApiError(
    typeof error?.message === 'string' && error.message.trim() ? error.message : fallback,
    response.status,
    typeof error?.code === 'number' ? error.code : undefined,
    Object.fromEntries([...fields].map(([field, messages]) => [field, [...messages].join(' ')])),
  );
}
