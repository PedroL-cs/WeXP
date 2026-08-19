import styles from './styles.module.css';

function Header() {
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

        <div className={styles['header-actions']}>
          <button className={styles['header-button']} aria-label='Notificações'>
            🔔
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
