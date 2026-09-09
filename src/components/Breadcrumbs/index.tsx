import { CaretRightIcon, HouseIcon } from '@phosphor-icons/react';
import { Link, useLocation } from 'react-router';

import styles from './styles.module.css';

type BreadcrumbItem = {
  label: string;
  path?: string;
};

function getBreadcrumbs(pathname: string): BreadcrumbItem[] {
  const segments = pathname.split('/').filter(Boolean);

  // /games/:gameId
  if (segments[0] === 'games' && segments[1] && segments.length === 2) {
    return [
      {
        label: 'Jogo',
      },
    ];
  }

  // /games/:gameId/achievements/:achievementId/guide
  if (
    segments[0] === 'games' &&
    segments[1] &&
    segments[2] === 'achievements' &&
    segments[3] &&
    segments[4] === 'guide' &&
    segments.length === 5
  ) {
    return [
      {
        label: 'Jogo',
        path: `/games/${segments[1]}`,
      },
      {
        label: 'Conquista',
      },
    ];
  }

  // /games/:gameId/achievements/:achievementId/guide/create
  if (
    segments[0] === 'games' &&
    segments[1] &&
    segments[2] === 'achievements' &&
    segments[3] &&
    segments[4] === 'guide' &&
    segments[5] === 'create'
  ) {
    return [
      {
        label: 'Jogo',
        path: `/games/${segments[1]}`,
      },
      {
        label: 'Conquista',
        path: `/games/${segments[1]}/achievements/${segments[3]}/guide`,
      },
      {
        label: 'Criar guia',
      },
    ];
  }

  // /games/:gameId/achievements/:achievementId/revise
  if (
    segments[0] === 'games' &&
    segments[1] &&
    segments[2] === 'achievements' &&
    segments[3] &&
    segments[4] === 'revise'
  ) {
    return [
      {
        label: 'Jogo',
        path: `/games/${segments[1]}`,
      },
      {
        label: 'Conquista',
        path: `/games/${segments[1]}/achievements/${segments[3]}/guide`,
      },
      {
        label: 'Sugerir revisão',
      },
    ];
  }

  const labels: Record<string, string> = {
    search: 'Busca',
    profile: 'Perfil',
    users: 'Usuário',
  };

  const label = labels[segments[0]];

  return label ? [{ label }] : [];
}

function Breadcrumbs() {
  const { pathname } = useLocation();
  const items = getBreadcrumbs(pathname);

  if (items.length === 0) return null;

  return (
    <nav className={styles.breadcrumbs} aria-label='Navegação estrutural'>
      <Link to='/' className={styles.link} aria-label='Página inicial'>
        <HouseIcon size={17} weight='fill' />
        <span>Home</span>
      </Link>

      {items.map((item, index) => {
        const isCurrent = index === items.length - 1;

        return (
          <span className={styles.item} key={`${item.label}-${index}`}>
            <CaretRightIcon className={styles.separator} size={14} />

            {isCurrent || !item.path ? (
              <span
                className={isCurrent ? styles.current : styles.link}
                {...(isCurrent ? { 'aria-current': 'page' } : {})}
              >
                {item.label}
              </span>
            ) : (
              <Link to={item.path} className={styles.link}>
                {item.label}
              </Link>
            )}
          </span>
        );
      })}
    </nav>
  );
}

export default Breadcrumbs;
