import { type FormEvent, useState } from 'react';

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
    <div>
      <h1>Entrar</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Usuário</label>
          <input
            type='text'
            value={username}
            onChange={event => setUsername(event.target.value)}
          />
        </div>
        <div>
          <label>Senha</label>
          <input
            type='password'
            value={password}
            onChange={event => setPassword(event.target.value)}
          />
        </div>
        <button type='submit'> Entrar </button>
      </form>
    </div>
  );
}
export default LoginPage;
