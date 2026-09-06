import {
  BellIcon,
  MagnifyingGlassIcon,
  XCircleIcon,
} from '@phosphor-icons/react';
import { useEffect, useState, useRef } from 'react';
import { useNavigate, Link } from 'react-router';
import styles from './styles.module.css';
import type { SteamGame } from '../../types/SteamGame';
import { instantSearchGames } from '../../api/games';
import Logo from '../Logo';
import { useAuth } from '../../contexts/AuthContext';

function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  const [search, setSearch] = useState('');
  const [searchResults, setSearchResults] = useState<SteamGame[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  const searchContainerRef = useRef<HTMLFormElement>(null);

  function handleGoHome() {
    setSearch('');
    setIsSearchOpen(false);
    navigate('/');
  }

  // Busca os jogos com debounce
  useEffect(() => {
    const query = search.trim();

    if (!query) {
      setSearchResults([]);
      return;
    }

    const timeout = setTimeout(async () => {
      try {
        setIsSearching(true);
        const results = await instantSearchGames(query);
        setSearchResults(results ?? []);
      } catch (error) {
        console.error('Erro na busca:', error);
        setSearchResults([]);
      } finally {
        setIsSearching(false);
      }
    }, 300);

    return () => clearTimeout(timeout);
  }, [search]);

  // Fecha o dropdown ao clicar fora do container de busca
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        searchContainerRef.current &&
        !searchContainerRef.current.contains(event.target as Node)
      ) {
        setIsSearchOpen(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  function handleSearchSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const query = search.trim();

    if (!query) return;

    setIsSearchOpen(false);
    navigate(`/search?q=${encodeURIComponent(query)}`);
  }

  function handleSearchChange(value: string) {
    setSearch(value);
    setIsSearchOpen(value.trim().length > 0);
  }

  function handleSelectGame(gameId: number | string) {
    setIsSearchOpen(false);
    setSearch('');
    navigate(`/games/${gameId}`);
  }

  return (
    <header className={styles.header}>
      <div className={`${styles.container} ${styles['header-content']}`}>
        {/* Logo */}
        <Logo onClick={handleGoHome} />
        {/* Form de Pesquisa */}
        <form
          ref={searchContainerRef}
          className={styles['search-container']}
          onSubmit={handleSearchSubmit}
        >
          <input
            type='search'
            value={search}
            onChange={e => handleSearchChange(e.target.value)}
            onFocus={() => {
              if (search.trim()) setIsSearchOpen(true);
            }}
            placeholder='Pesquisar jogos...'
            aria-label='Pesquisar jogos'
            className={styles['search-input']}
          />

          {search && (
            <button
              type='button'
              className={styles['search-clear']}
              onClick={() => {
                setSearch('');
                setIsSearchOpen(false);
              }}
              aria-label='Limpar pesquisa'
            >
              <XCircleIcon size={19} />
            </button>
          )}

          <button
            type='submit'
            className={styles['search-submit']}
            aria-label='Pesquisar'
          >
            <MagnifyingGlassIcon size={19} />
          </button>

          {/* Dropdown de Resultados Instantâneos */}
          {isSearchOpen && (
            <div className={styles['search-dropdown']}>
              {isSearching ? (
                <div className={styles['search-status']}>Buscando...</div>
              ) : searchResults.length === 0 ? (
                <div className={styles['search-status']}>
                  Nenhum jogo encontrado
                </div>
              ) : (
                searchResults.map(game => (
                  <button
                    key={game.appid}
                    type='button'
                    className={styles['search-result']}
                    onClick={() => handleSelectGame(game.appid)}
                  >
                    <div className={styles['search-result-image']}>
                      <img src={game.capsuleUrl} alt={game.name} />
                    </div>
                    <span>{game.name}</span>
                  </button>
                ))
              )}
            </div>
          )}
        </form>

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
