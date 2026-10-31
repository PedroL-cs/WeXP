import {
  type ChangeEvent,
  type ClipboardEvent,
  type FormEvent,
  useLayoutEffect,
  useRef,
  useState,
} from 'react';
import { Link, useNavigate } from 'react-router';
import styles from './styles.module.css';
import Logo from '../../components/Logo';
import AuthShowcase from '../../components/AuthShowcase';
import { register as registerRequest } from '../../api/auth';
import { useAuth } from '../../contexts/AuthContext';
import { authFeedback, focusAuthField, validateRegistration, type AuthField, type AuthFieldErrors } from '../../utils/authValidation';
import {
  birthDateCursor,
  birthDateDigits,
  formatBirthDate,
  getBirthDateError,
  maskBirthDateChange,
} from '../../utils/birthDate';

function RegisterPage() {
  const navigate = useNavigate();
  const { login: authenticate } = useAuth();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [dateTouched, setDateTouched] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<AuthFieldErrors>({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const dateInput = useRef<HTMLInputElement>(null);
  const dateSelection = useRef<number | null>(null);
  const birthDateError = fieldErrors.birthDate || (dateTouched ? getBirthDateError(birthDate) : '');

  function clearError(field: AuthField) {
    setFieldErrors(current => ({ ...current, [field]: undefined }));
    setFormError('');
  }

  useLayoutEffect(() => {
    if (dateSelection.current === null) return;
    dateInput.current?.setSelectionRange(
      dateSelection.current,
      dateSelection.current,
    );
    dateSelection.current = null;
  }, [birthDate]);

  function updateBirthDate(
    input: HTMLInputElement,
    value: string,
    cursor: number,
  ) {
    dateSelection.current = value === birthDate ? null : cursor;
    // Keep the caret correct even when an ignored character leaves state unchanged.
    input.value = value;
    input.setSelectionRange(cursor, cursor);
    setBirthDate(value);
    clearError('birthDate');
  }

  function changeBirthDate(event: ChangeEvent<HTMLInputElement>) {
    const input = event.currentTarget;
    const masked = maskBirthDateChange(
      birthDate,
      input.value,
      input.selectionStart ?? input.value.length,
      (event.nativeEvent as InputEvent).inputType,
    );
    updateBirthDate(input, masked.value, masked.cursor);
  }

  function pasteBirthDate(event: ClipboardEvent<HTMLInputElement>) {
    event.preventDefault();
    const input = event.currentTarget;
    const pasted = birthDateDigits(event.clipboardData.getData('text'));
    if (!pasted) return;
    const digits = birthDateDigits(birthDate);
    const start = birthDateDigits(
      birthDate.slice(0, input.selectionStart ?? 0),
    ).length;
    const end = birthDateDigits(
      birthDate.slice(0, input.selectionEnd ?? birthDate.length),
    ).length;
    const value = formatBirthDate(
      digits.slice(0, start) + pasted + digits.slice(end),
    );
    updateBirthDate(
      input,
      value,
      birthDateCursor(value, start + pasted.length),
    );
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (submitting) return;
    setDateTouched(true);
    const errors = validateRegistration({ username, email, login, password, birthDate });
    setFieldErrors(errors);
    setFormError('');
    if (Object.keys(errors).length) {
      focusAuthField(errors, ['username', 'email', 'login', 'password', 'birthDate']);
      return;
    }

    setSubmitting(true);
    try {
      const data = await registerRequest(
        username,
        email,
        login,
        password,
        birthDate,
      );

      authenticate(data.token, true);
      navigate('/');
    } catch (error) {
      const feedback = authFeedback(error, ['username', 'email', 'login', 'password', 'birthDate']);
      setFieldErrors(feedback.fieldErrors);
      setFormError(feedback.message);
      focusAuthField(feedback.fieldErrors, ['username', 'email', 'login', 'password', 'birthDate']);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.registerPage}>
      <AuthShowcase
        title='Comece sua jornada.'
        description='Crie sua conta e descubra jogos, conquistas e guias em um só lugar.'
      />

      <section className={styles.registerSection}>
        <div className={styles.registerContainer}>
          <div className={styles.logoContainer}>
            <Logo size='lg' onClick={() => navigate('/')} />
          </div>

          <h2>Crie sua conta</h2>

          <p className={styles.subtitle}>Preencha seus dados para começar.</p>

          <form onSubmit={handleSubmit} className={styles.form} noValidate aria-busy={submitting}>
            {formError && <p className={styles.formError} role='alert'>{formError}</p>}
            <div className={styles.inputGroup}>
              <label htmlFor='username'>Nome de exibição</label>

              <input
                id='username'
                type='text'
                autoComplete='nickname'
                required
                maxLength={100}
                aria-invalid={Boolean(fieldErrors.username)}
                aria-describedby={fieldErrors.username ? 'username-error' : undefined}
                value={username}
                placeholder='Como você aparece no site'
                onChange={event => { setUsername(event.target.value); clearError('username'); }}
              />
              {fieldErrors.username && <p id='username-error' className={styles.fieldError} role='alert'>{fieldErrors.username}</p>}
            </div>

            <div className={styles.inputGroup}>
              <label htmlFor='email'>E-mail</label>

              <input
                id='email'
                type='email'
                autoComplete='email'
                required
                aria-invalid={Boolean(fieldErrors.email)}
                aria-describedby={fieldErrors.email ? 'email-error' : undefined}
                value={email}
                placeholder='Digite seu e-mail'
                onChange={event => { setEmail(event.target.value); clearError('email'); }}
              />
              {fieldErrors.email && <p id='email-error' className={styles.fieldError} role='alert'>{fieldErrors.email}</p>}
            </div>

            <div className={styles.inputGroup}>
              <label htmlFor='login'>Usuário</label>

              <input
                id='login'
                type='text'
                autoComplete='username'
                required
                minLength={3}
                maxLength={30}
                aria-invalid={Boolean(fieldErrors.login)}
                aria-describedby={fieldErrors.login ? 'login-hint login-error' : 'login-hint'}
                value={login}
                placeholder='Usado para entrar na sua conta'
                onChange={event => { setLogin(event.target.value); clearError('login'); }}
              />
              <p id='login-hint' className={styles.inputHint}>Use de 3 a 30 caracteres.</p>
              {fieldErrors.login && <p id='login-error' className={styles.fieldError} role='alert'>{fieldErrors.login}</p>}
            </div>

            <div className={styles.formRow}>
              <div className={styles.inputGroup}>
                <label htmlFor='password'>Senha</label>

                <input
                  id='password'
                  type='password'
                  autoComplete='new-password'
                  required
                  minLength={8}
                  aria-invalid={Boolean(fieldErrors.password)}
                  aria-describedby={fieldErrors.password ? 'password-hint password-error' : 'password-hint'}
                  value={password}
                  placeholder='Digite sua senha'
                  onChange={event => { setPassword(event.target.value); clearError('password'); }}
                />
                <p id='password-hint' className={styles.inputHint}>Use pelo menos 8 caracteres.</p>
                {fieldErrors.password && <p id='password-error' className={styles.fieldError} role='alert'>{fieldErrors.password}</p>}
              </div>

              <div className={styles.inputGroup}>
                <label htmlFor='birthDate'>Data de nascimento</label>

                <input
                  id='birthDate'
                  ref={dateInput}
                  type='text'
                  placeholder='dd/mm/aaaa'
                  inputMode='numeric'
                  autoComplete='bday'
                  maxLength={10}
                  required
                  aria-invalid={Boolean(birthDateError)}
                  aria-describedby={
                    birthDateError
                      ? 'birthDateError'
                      : undefined
                  }
                  value={birthDate}
                  onChange={changeBirthDate}
                  onPaste={pasteBirthDate}
                  onBlur={() => setDateTouched(true)}
                  onInvalid={event => {
                    event.preventDefault();
                    setDateTouched(true);
                    dateInput.current?.focus();
                  }}
                />
                {birthDateError && (
                  <p
                    id='birthDateError'
                    className={styles.fieldError}
                    role='alert'
                  >
                    {birthDateError}
                  </p>
                )}
              </div>
            </div>

            <button type='submit' className={styles.registerButton} disabled={submitting}>
              {submitting ? 'Criando conta...' : 'Criar conta'}
            </button>
          </form>

          <p className={styles.login}>
            Já possui uma conta? <Link to='/login'>Entrar</Link>
          </p>
        </div>
      </section>
    </div>
  );
}

export default RegisterPage;
