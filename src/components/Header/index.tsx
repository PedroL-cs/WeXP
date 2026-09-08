import { BellIcon } from '@phosphor-icons/react';
import { useNavigate, Link } from 'react-router';
import styles from './styles.module.css';
import Logo from '../Logo';
import { useAuth } from '../../contexts/AuthContext';
import SearchBar from '../SearchBar';

type HeaderProps = {
  search: string;
  setSearch: React.Dispatch<React.SetStateAction<string>>;
  isSearchOpen: boolean;
  setIsSearchOpen: React.Dispatch<React.SetStateAction<boolean>>;
};

function Header({
  search,
  setSearch,
  isSearchOpen,
  setIsSearchOpen,
}: HeaderProps) {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  function handleGoHome() {
    setSearch('');
    setIsSearchOpen(false);
    navigate('/');
  }

  return (
    <header className={styles.header}>
      <div className={`${styles.container} ${styles['header-content']}`}>
        {/* Logo */}
        <Logo onClick={handleGoHome} />

        <SearchBar
          search={search}
          setSearch={setSearch}
          isSearchOpen={isSearchOpen}
          setIsSearchOpen={setIsSearchOpen}
        />

        {/* Ações de Perfil / Notificação */}
        <div className={styles['header-actions']}>
          {isAuthenticated && (
            <button
              className={styles['header-button']}
              aria-label='Notificações'
            >
              <BellIcon size={22} />
            </button>
          )}

          {isAuthenticated ? (
            <div className={styles['profile-area']}>
              <button className={styles['profile-button']} aria-label='Perfil'>
                <div className={styles['profile-avatar']}>P</div>
              </button>
            </div>
          ) : (
            <div className={styles['auth-buttons']}>
              <Link to='/login' className={styles['login-link']}>
                Logar
              </Link>

              <Link to='/register' className={styles['register-link']}>
                Cadastrar
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
