import {
  BookBookmarkIcon,
  BookOpenIcon,
  GameControllerIcon,
  HouseIcon,
  InfoIcon,
  SignOutIcon,
  TrophyIcon,
} from '@phosphor-icons/react';
import styles from './styles.module.css';
import { useAuth } from '../../contexts/AuthContext';
import { NavLink } from 'react-router';

function Sidebar() {
  const { isAuthenticated, logout } = useAuth();

  const itemClass = ({ isActive }: { isActive: boolean }) =>
    `${styles['navigation-item']} ${isActive ? styles.active : ''}`;

  return (
    <aside className={styles.sidebar}>
      <nav className={styles.navigation} aria-label='Navegação principal'>
        <NavLink to='/' end className={itemClass} title='Home'>
          <HouseIcon size={22} />
          <span>Home</span>
        </NavLink>

        <NavLink
          to='/library'
          end
          className={({ isActive }) =>
            `${styles['navigation-item']} ${styles.secondary} ${isActive ? styles.active : ''}`
          }
          title='Biblioteca'
        >
          <BookBookmarkIcon size={22} />
          <span>Biblioteca</span>
        </NavLink>

        <div className={styles.divider}></div>

        <NavLink to='/library/games' className={itemClass} title='Jogos'>
          <GameControllerIcon size={22} />
          <span>Jogos</span>
        </NavLink>

        <NavLink
          to='/library/achievements'
          className={itemClass}
          title='Conquistas'
        >
          <TrophyIcon size={22} />
          <span>Conquistas</span>
        </NavLink>

        <NavLink to='/library/guides' className={itemClass} title='Guias'>
          <BookOpenIcon size={22} />
          <span>Guias</span>
        </NavLink>

        <div className={styles.divider}></div>

        <NavLink
          to='/about'
          className={`${styles['navigation-item']} ${styles.secondary}`}
          title='Sobre'
        >
          <InfoIcon size={22} />
          <span>Sobre</span>
        </NavLink>

        {isAuthenticated && (
          <button
            type='button'
            className={`${styles['navigation-item']} ${styles['logout-item']}`}
            onClick={logout}
            title='Sair'
          >
            <SignOutIcon size={22} />
            <span>Sair</span>
          </button>
        )}
      </nav>
    </aside>
  );
}

export default Sidebar;
