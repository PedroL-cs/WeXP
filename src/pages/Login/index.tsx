import { type FormEvent, useState } from 'react';
import styles from './styles.module.css';
import { Link } from 'react-router';

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();

    const response = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });

    const data = await response.json();

    console.log('Status:', response.status);
    console.log('Resposta:', data);

    if (response.ok) {
      localStorage.setItem('token', data.token);
      alert('Login realizado com sucesso!');
    } else {
      alert('Usuário ou senha inválidos.');
    }
  }

  return (
    <div className={styles.loginPage}>
      <section className={styles.showcase}>
        <div className={styles.showcaseOverlay}>
          <h1>Explore novos mundos.</h1>
          <p>Descubra jogos, conquistas e guias em um só lugar.</p>
        </div>
      </section>

      <section className={styles.loginSection}>
        <div className={styles.loginContainer}>
          <div className={styles.logo}>
            We<span>XP</span>
          </div>

          <h2>Bem-vindo de volta!</h2>

          <p className={styles.subtitle}>Entre na sua conta para continuar.</p>

          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.inputGroup}>
              <label htmlFor='username'>Usuário</label>

              <input
                id='username'
                type='text'
                value={username}
                placeholder='Digite seu usuário'
                onChange={event => setUsername(event.target.value)}
              />
            </div>

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

            {/* <div className={styles.formOptions}>
              <label className={styles.remember}>
                <input type='checkbox' />
                <span>Lembrar-me</span>
              </label>

              <a href='#'>Esqueceu sua senha?</a>
            </div> */}

            <button type='submit' className={styles.loginButton}>
              Entrar
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
