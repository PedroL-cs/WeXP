import { type FormEvent, useState } from 'react';

function RegisterPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [fullName, setFullName] = useState('');
  const [password, setPassword] = useState('');
  const [birthDate, setBirthDate] = useState('');

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();

    const response = await fetch('/api/v1/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username,
        email,
        fullName,
        password,
        birthDate,
      }),
    });

    const data = await response.json();

    console.log('Status:', response.status);
    console.log('Resposta:', data);

    if (response.ok) {
      localStorage.setItem('token', data.token);
      alert('Usuário criado com sucesso!');
    } else {
      alert('Erro ao criar usuário.');
    }
  }

  return (
    <div>
      <h1>Criar conta</h1>

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
          <label>E-mail</label>
          <input
            type='email'
            value={email}
            onChange={event => setEmail(event.target.value)}
          />
        </div>

        <div>
          <label>Nome completo</label>
          <input
            type='text'
            value={fullName}
            onChange={event => setFullName(event.target.value)}
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

        <div>
          <label>Data de nascimento</label>
          <input
            type='text'
            placeholder='dd/MM/yyyy'
            value={birthDate}
            onChange={event => setBirthDate(event.target.value)}
          />
        </div>

        <button type='submit'>Cadastrar</button>
      </form>
    </div>
  );
}

export default RegisterPage;
