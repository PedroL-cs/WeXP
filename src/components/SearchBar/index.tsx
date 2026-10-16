import { useEffect, useRef, useState } from 'react';
import styles from './styles.module.css';
import type { SteamGame } from '../../types/SteamGame';
import { MagnifyingGlassIcon, XCircleIcon } from '@phosphor-icons/react';
import { getGameIdBySteamAppId, instantSearchGames } from '../../api/games';
import { useNavigate } from 'react-router';

type SearchBarProps = {
  search: string;
  setSearch: React.Dispatch<React.SetStateAction<string>>;
  isSearchOpen: boolean;
  setIsSearchOpen: React.Dispatch<React.SetStateAction<boolean>>;
};

function SearchBar({
  search,
  setSearch,
  isSearchOpen,
  setIsSearchOpen,
}: SearchBarProps) {
  const navigate = useNavigate();

  const searchContainerRef = useRef<HTMLFormElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const [searchResults, setSearchResults] = useState<SteamGame[]>([]);
  const [isSearching, setIsSearching] = useState(false);

  // Busca os jogos com debounce
  useEffect(() => {
    const query = search.trim();
    const controller = new AbortController();

    if (!query) {
      setSearchResults([]);
      return;
    }

    const timeout = setTimeout(async () => {
      try {
        setIsSearching(true);
        const results = await instantSearchGames(query, controller.signal);
        setSearchResults(results ?? []);
      } catch (error) {
        if (error instanceof DOMException && error.name === 'AbortError') return;
        console.error('Erro na busca:', error);
        setSearchResults([]);
      } finally {
        setIsSearching(false);
      }
    }, 300);

    return () => {
      clearTimeout(timeout);
      controller.abort();
    };
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
  }, [setIsSearchOpen]);

  // Atalho "/" foca a busca, como em GitHub e YouTube
  useEffect(() => {
    function handleShortcut(event: KeyboardEvent) {
      const target = event.target as HTMLElement;
      const isTyping =
        target.isContentEditable ||
        ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName);

      if (event.key === '/' && !isTyping) {
        event.preventDefault();
        inputRef.current?.focus();
      }
    }

    document.addEventListener('keydown', handleShortcut);
    return () => document.removeEventListener('keydown', handleShortcut);
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

  async function handleSelectGame(steamAppId: number) {
    try {
      const data = await getGameIdBySteamAppId(steamAppId);

      setIsSearchOpen(false);
      setSearch('');

      navigate(`/games/${data.gameId}`);
    } catch (error) {
      console.error('Erro ao buscar jogo:', error);
    }
  }

  return (
    <form
      ref={searchContainerRef}
      className={styles['search-container']}
      onSubmit={handleSearchSubmit}
    >
      <MagnifyingGlassIcon className={styles['search-icon']} size={18} />

      <input
        ref={inputRef}
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

      {!search && (
        <kbd className={styles['search-kbd']} aria-hidden='true'>
          /
        </kbd>
      )}

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

      {/* Dropdown de Resultados Instantâneos */}
      {isSearchOpen && (
        <div className={styles['search-dropdown']}>
          {isSearching ? (
            <div className={styles['search-status']}>
              <span className={styles.spinner} />
              Buscando...
            </div>
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
  );
}

export default SearchBar;
