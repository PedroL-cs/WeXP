import { BellIcon, MagnifyingGlassIcon } from '@phosphor-icons/react';
import { useState } from 'react';
import styles from './styles.module.css';

function Header() {
  const [search, setSearch] = useState('');

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

        <div className={styles['search-container']}>
          <MagnifyingGlassIcon
            className={styles['search-icon']}
            size={20}
          />

          <input
            type='search'
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder='Pesquisar jogos...'
            aria-label='Pesquisar jogos'
            className={styles['search-input']}
          />

          {search && (
            <button
              type='button'
              className={styles['search-clear']}
              onClick={() => setSearch('')}
              aria-label='Limpar pesquisa'
            >
              ×
            </button>
          )}
        </div>

        <div className={styles['header-actions']}>
          <button className={styles['header-button']} aria-label='Notificações'>
            <BellIcon size={22} />
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