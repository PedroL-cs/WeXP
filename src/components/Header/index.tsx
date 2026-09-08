import { BellIcon } from '@phosphor-icons/react';
import styles from './styles.module.css';

<<<<<<< Updated upstream
function Header() {
=======
  async function testaAuth() {
    const token = localStorage.getItem('token');

    const response = await fetch('/api/v1/users/me', {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const data = await response.json();

    console.log(data);
  }

>>>>>>> Stashed changes
  return (
    <header className={styles.header}>
      <div className={`${styles.container} ${styles['header-content']}`}>
        <div className={styles['logo-container']}>
          <div className={styles['wasd-icon']} aria-label='Ícone WASD'>
            <div className={`${styles.key} ${styles['key-w']}`}></div>
            <div className={`${styles.key} ${styles['key-a']}`}></div>
            <div className={`${styles.key} ${styles['key-s']}`}></div>
            <div className={`${styles.key} ${styles['key-d']}`}></div>
          </div>

          <h1 className={styles.logo}>WeXP</h1>
        </div>

<<<<<<< Updated upstream
=======
        <button onClick={testaAuth}>Testar auth</button>

        {/* Ações de Perfil / Notificação */}
>>>>>>> Stashed changes
        <div className={styles['header-actions']}>
          <button className={styles['header-button']} aria-label='Notificações'>
            <BellIcon size={22} />
            {/* <BellRingingIcon size={22} weight='fill' /> */}
          </button>

          <button className={styles['profile-button']} aria-label='Perfil'>
            <div className={styles['profile-avatar']}>P</div>
          </button>
        </div>
      </div>
    </header>
  );
}

export default Header;
