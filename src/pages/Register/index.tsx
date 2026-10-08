import { type FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router';
import styles from './styles.module.css';
import Logo from '../../components/Logo';
import AuthShowcase from '../../components/AuthShowcase';
import { register as registerRequest } from '../../api/auth';
import { useAuth } from '../../contexts/AuthContext';

function RegisterPage() {
  const navigate = useNavigate();
  const { login: authenticate } = useAuth();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [birthDate, setBirthDate] = useState('');

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();

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
      alert(error instanceof Error ? error.message : 'Erro ao criar usuário.');
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

          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.inputGroup}>
              <label htmlFor='username'>Nome de exibição</label>

              <input
                id='username'
                type='text'
                value={username}
                placeholder='Como você aparece no site'
                onChange={event => setUsername(event.target.value)}
              />
            </div>

            <div className={styles.inputGroup}>
              <label htmlFor='email'>E-mail</label>

              <input
                id='email'
                type='email'
                value={email}
                placeholder='Digite seu e-mail'
                onChange={event => setEmail(event.target.value)}
              />
            </div>

            <div className={styles.inputGroup}>
              <label htmlFor='login'>Usuário</label>

              <input
                id='login'
                type='text'
                value={login}
                placeholder='Usado para entrar na sua conta'
                onChange={event => setLogin(event.target.value)}
              />
            </div>

            <div className={styles.formRow}>
              <div className={styles.inputGroup}>
                <label htmlFor='password'>Senha</label>

                <input
                  id='password'
                  type='password'
                  value={password}
                  placeholder='Digite sua senha'
                  onChange={event => setPassword(event.target.value)}
                />
              </div>

              <div className={styles.inputGroup}>
                <label htmlFor='birthDate'>Data de nascimento</label>

                <input
                  id='birthDate'
                  type='text'
                  placeholder='dd/mm/aaaa'
                  inputMode='numeric'
                  value={birthDate}
                  onChange={event => setBirthDate(event.target.value)}
                />
              </div>
            </div>

            <button type='submit' className={styles.registerButton}>
              Criar conta
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
