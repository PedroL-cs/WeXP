import {
  BookBookmarkIcon,
  BookOpenIcon,
  GameControllerIcon,
  GearSixIcon,
  HouseIcon,
  InfoIcon,
  TrophyIcon,
} from '@phosphor-icons/react';
import styles from './styles.module.css';

function Sidebar() {
  return (
    <aside className={styles.sidebar}>
      <nav className={styles.navigation}>
        <a href='#' className={styles['navigation-item']}>
          <HouseIcon size={22} />
          <span>Home</span>
        </a>

        <a href='#' className={styles['navigation-item']}>
          <BookBookmarkIcon size={22} />
          <span>Biblioteca</span>
        </a>

        <div className={styles.divider}></div>

        <a href='#' className={styles['navigation-item']}>
          <GameControllerIcon size={22} />
          <span>Jogos</span>
        </a>

        <a href='#' className={styles['navigation-item']}>
          <TrophyIcon size={22} />
          <span>Conquistas</span>
        </a>

        <a href='#' className={styles['navigation-item']}>
          <BookOpenIcon size={22} />
          <span>Guias</span>
        </a>

        <div className={styles.divider}></div>

        <a href='#' className={styles['navigation-item']}>
          <GearSixIcon size={22} />
          <span>Opções</span>
        </a>

        <a href='#' className={styles['navigation-item']}>
          <InfoIcon size={22} />
          <span>Sobre</span>
        </a>
      </nav>
    </aside>
  );
}

export default Sidebar;
