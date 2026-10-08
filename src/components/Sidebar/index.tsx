import {
  BookBookmarkIcon,
  BookOpenIcon,
  GameControllerIcon,
  GearSixIcon,
  HouseIcon,
  InfoIcon,
  SignOutIcon,
  TrophyIcon,
} from '@phosphor-icons/react';
import styles from './styles.module.css';
import { useAuth } from '../../contexts/AuthContext';
import { Link, NavLink } from 'react-router';

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

        <Link
          to='#'
          className={`${styles['navigation-item']} ${styles.secondary}`}
          title='Biblioteca'
        >
          <BookBookmarkIcon size={22} />
          <span>Biblioteca</span>
        </Link>

        <div className={styles.divider}></div>

        <Link to='#' className={styles['navigation-item']} title='Jogos'>
          <GameControllerIcon size={22} />
          <span>Jogos</span>
        </Link>

        <Link to='#' className={styles['navigation-item']} title='Conquistas'>
          <TrophyIcon size={22} />
          <span>Conquistas</span>
        </Link>

        <Link to='#' className={styles['navigation-item']} title='Guias'>
          <BookOpenIcon size={22} />
          <span>Guias</span>
        </Link>

        <div className={styles.divider}></div>

        <Link
          to='#'
          className={`${styles['navigation-item']} ${styles.secondary}`}
          title='Opções'
        >
          <GearSixIcon size={22} />
          <span>Opções</span>
        </Link>

        <Link
          to='#'
          className={`${styles['navigation-item']} ${styles.secondary}`}
          title='Sobre'
        >
          <InfoIcon size={22} />
          <span>Sobre</span>
        </Link>

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
