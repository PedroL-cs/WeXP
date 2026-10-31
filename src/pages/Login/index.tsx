import { type FormEvent, useState } from 'react';
import styles from './styles.module.css';
import { Link, useLocation, useNavigate } from 'react-router';
import Logo from '../../components/Logo';
import AuthShowcase from '../../components/AuthShowcase';
import { useAuth } from '../../contexts/AuthContext';
import { login as loginRequest } from '../../api/auth';
import { authFeedback, focusAuthField, validateLogin, type AuthFieldErrors } from '../../utils/authValidation';

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<AuthFieldErrors>({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { login } = useAuth();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (submitting) return;
    const errors = validateLogin(username, password);
    setFieldErrors(errors);
    setFormError('');
    if (Object.keys(errors).length) {
      focusAuthField(errors, ['login', 'password']);
      return;
    }

    setSubmitting(true);
    try {
      const data = await loginRequest(username, password);

      login(data.token, rememberMe);
      const from = location.state?.from;
      navigate(
        typeof from === 'string' &&
          from.startsWith('/') &&
          !from.startsWith('//')
          ? from
          : '/',
        { replace: true },
      );
    } catch (error) {
      const feedback = authFeedback(error, ['login', 'password']);
      setFieldErrors(feedback.fieldErrors);
      setFormError(feedback.message);
      focusAuthField(feedback.fieldErrors, ['login', 'password']);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.loginPage}>
      <AuthShowcase
        title='Explore novos mundos.'
        description='Descubra jogos, conquistas e guias em um só lugar.'
      />

      <section className={styles.loginSection}>
        <div className={styles.loginContainer}>
          <div className={styles.logoContainer}>
            <Logo size='lg' onClick={() => navigate('/')} />
          </div>

          <h2>Bem-vindo de volta!</h2>

          <p className={styles.subtitle}>Entre na sua conta para continuar.</p>

          <form onSubmit={handleSubmit} className={styles.form} noValidate aria-busy={submitting}>
            {formError && <p className={styles.formError} role='alert'>{formError}</p>}
            <div className={styles.inputGroup}>
              <label htmlFor='login'>Usuário</label>

              <input
                id='login'
                type='text'
                autoComplete='username'
                required
                aria-invalid={Boolean(fieldErrors.login)}
                aria-describedby={fieldErrors.login ? 'login-error' : undefined}
                value={username}
                placeholder='Digite seu usuário'
                onChange={event => {
                  setUsername(event.target.value);
                  setFieldErrors(current => ({ ...current, login: undefined }));
                  setFormError('');
                }}
              />
              {fieldErrors.login && <p id='login-error' className={styles.fieldError} role='alert'>{fieldErrors.login}</p>}
            </div>

            <div className={styles.inputGroup}>
              <label htmlFor='password'>Senha</label>

              <input
                id='password'
                type='password'
                autoComplete='current-password'
                required
                aria-invalid={Boolean(fieldErrors.password)}
                aria-describedby={fieldErrors.password ? 'password-error' : undefined}
                value={password}
                placeholder='Digite sua senha'
                onChange={event => {
                  setPassword(event.target.value);
                  setFieldErrors(current => ({ ...current, password: undefined }));
                  setFormError('');
                }}
              />
              {fieldErrors.password && <p id='password-error' className={styles.fieldError} role='alert'>{fieldErrors.password}</p>}
            </div>

            <div className={styles.formOptions}>
              <label className={styles.remember}>
                <input
                  type='checkbox'
                  checked={rememberMe}
                  onChange={event => setRememberMe(event.target.checked)}
                />

                <span>Lembrar-me</span>
              </label>

              {/* <Link to='/forgot-password'>Esqueceu sua senha?</Link> */}
            </div>

            <button type='submit' className={styles.loginButton} disabled={submitting}>
              {submitting ? 'Entrando...' : 'Entrar'}
            </button>
          </form>

          <p className={styles.register}>
            Ainda não possui uma conta? <Link to='/register'>Cadastre-se</Link>
          </p>
        </div>
      </section>
    </div>
  );
}

export default LoginPage;
