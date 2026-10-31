import { ApiError } from '../api/apiError';
import { getBirthDateError } from './birthDate';

export type AuthField = 'username' | 'email' | 'login' | 'password' | 'birthDate';
export type AuthFieldErrors = Partial<Record<AuthField, string>>;

export function validateLogin(login: string, password: string): AuthFieldErrors {
  return {
    ...(!login.trim() ? { login: 'Informe seu usuário.' } : {}),
    ...(!password.trim() ? { password: 'Informe sua senha.' } : {}),
  };
}

export function validateRegistration(values: Record<AuthField, string>): AuthFieldErrors {
  const errors = validateLogin(values.login, values.password);
  if (!errors.login && (values.login.length < 3 || values.login.length > 30)) {
    errors.login = 'O usuário deve ter entre 3 e 30 caracteres.';
  }
  if (!values.username.trim()) errors.username = 'Informe seu nome de exibição.';
  else if (values.username.length > 100) errors.username = 'O nome de exibição deve ter no máximo 100 caracteres.';
  if (!values.email.trim()) errors.email = 'Informe seu e-mail.';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) errors.email = 'Informe um e-mail válido.';
  if (!errors.password && values.password.length < 8) errors.password = 'A senha deve ter no mínimo 8 caracteres.';
  const dateError = getBirthDateError(values.birthDate);
  if (dateError) errors.birthDate = dateError;
  return errors;
}

export function authFeedback(error: unknown, fields: AuthField[]) {
  const fieldErrors: AuthFieldErrors = {};
  if (error instanceof ApiError) {
    for (const field of fields) {
      if (Object.prototype.hasOwnProperty.call(error.fieldErrors, field)) {
        fieldErrors[field] = error.fieldErrors[field];
      }
    }
    if (error.code === 8 && fields.includes('email')) fieldErrors.email = 'Este e-mail já está cadastrado.';
    if (error.code === 9 && fields.includes('login')) fieldErrors.login = 'Este usuário já está sendo utilizado.';
    if (Object.keys(fieldErrors).length) {
      return { fieldErrors, message: 'Confira os campos indicados para continuar.' };
    }
    const details = Object.values(error.fieldErrors).join(' ');
    return { fieldErrors, message: details || error.message };
  }
  return { fieldErrors, message: 'Não foi possível concluir a solicitação. Tente novamente.' };
}

export function focusAuthField(errors: AuthFieldErrors, order: AuthField[]) {
  const field = order.find(field => errors[field]);
  if (field) document.getElementById(field)?.focus();
}
