export class ApiError extends Error {
  status: number;
  code?: number;

  constructor(message: string, status: number, code?: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

export async function throwApiError(
  response: Response,
  fallback: string,
): Promise<never> {
  const error = await response.json().catch(() => null);
  throw new ApiError(
    typeof error?.message === 'string' ? error.message : fallback,
    response.status,
    typeof error?.code === 'number' ? error.code : undefined,
  );
}
