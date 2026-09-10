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
import { Link } from 'react-router';

function Sidebar() {
  const { isAuthenticated, logout } = useAuth();

  return (
    <aside className={styles.sidebar}>
      <nav className={styles.navigation}>
        <Link to='/' className={styles['navigation-item']}>
          <HouseIcon size={22} />
          <span>Home</span>
        </Link>

        <Link to='#' className={styles['navigation-item']}>
          <BookBookmarkIcon size={22} />
          <span>Biblioteca</span>
        </Link>

        <div className={styles.divider}></div>

        <Link to='#' className={styles['navigation-item']}>
          <GameControllerIcon size={22} />
          <span>Jogos</span>
        </Link>

        <Link to='#' className={styles['navigation-item']}>
          <TrophyIcon size={22} />
          <span>Conquistas</span>
        </Link>

        <Link to='#' className={styles['navigation-item']}>
          <BookOpenIcon size={22} />
          <span>Guias</span>
        </Link>

        <div className={styles.divider}></div>

        <Link to='#' className={styles['navigation-item']}>
          <GearSixIcon size={22} />
          <span>Opções</span>
        </Link>

        <Link to='#' className={styles['navigation-item']}>
          <InfoIcon size={22} />
          <span>Sobre</span>
        </Link>

        {isAuthenticated && (
          <Link
            to='#'
            className={styles['navigation-item'] + ' ' + styles['logout-item']}
            onClick={logout}
          >
            <SignOutIcon size={22} />
            <span onClick={logout}>Sair</span>
          </Link>
        )}
      </nav>
    </aside>
  );
}

export default Sidebar;
