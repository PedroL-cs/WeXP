import { BellIcon } from '@phosphor-icons/react';
import { useNavigate, Link } from 'react-router';
import { useEffect, useState } from 'react';
import styles from './styles.module.css';
import Logo from '../Logo';
import { useAuth } from '../../contexts/AuthContext';
import SearchBar from '../SearchBar';
import { getCurrentUser } from '../../api/users';
import type { CurrentUser } from '../../types/User';

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
  const [user, setUser] = useState<CurrentUser | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      setUser(null);
      return;
    }

    let active = true;
    async function loadUser() {
      try {
        const profile = await getCurrentUser();
        if (active) setUser(profile);
      } catch {
        // O cabeçalho mantém o fallback visual caso o perfil não carregue.
      }
    }

    loadUser();
    window.addEventListener('user-profile-updated', loadUser);
    return () => {
      active = false;
      window.removeEventListener('user-profile-updated', loadUser);
    };
  }, [isAuthenticated]);

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
              <button
                className={styles['profile-button']}
                aria-label='Perfil'
                onClick={() => navigate('/profile')}
              >
                <div className={styles['profile-avatar']}>
                  {user?.avatar ? (
                    <img src={user.avatar} alt={`Foto de ${user.username}`} />
                  ) : (
                    user?.username?.charAt(0).toUpperCase() ?? 'P'
                  )}
                </div>
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
