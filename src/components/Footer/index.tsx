import {
  BookBookmarkIcon,
  GameControllerIcon,
  InfoIcon,
} from '@phosphor-icons/react';
import { Link, useNavigate } from 'react-router';

import Logo from '../Logo';
import styles from './styles.module.css';

function Footer() {
  const navigate = useNavigate();

  return (
    <footer className={styles.footer}>
      <div className={styles.glow} aria-hidden='true' />

      <div className={styles.content}>
        <div className={styles.brand}>
          <Logo size='sm' onClick={() => navigate('/')} />
          <p>
            Descubra jogos, acompanhe conquistas e compartilhe conhecimento
            construído pela comunidade.
          </p>
          <a
            href='https://forms.gle/onVH7CEDhzm4vKn36'
            target='_blank'
            rel='noopener noreferrer'
          >
            Nos dê a sua opinião!
          </a>
        </div>

        <nav className={styles.navigation} aria-label='Navegação do rodapé'>
          <span className={styles.title}>Navegação</span>
          <Link to='/'>
            <GameControllerIcon size={16} weight='fill' />
            Início
          </Link>
          <Link to='/library'>
            <BookBookmarkIcon size={16} weight='fill' />
            Biblioteca
          </Link>
          <Link to='/about'>
            <InfoIcon size={16} weight='fill' />
            Sobre o projeto
          </Link>
        </nav>

        <div className={styles.disclaimer}>
          <span className={styles.title}>Dados externos</span>
          <p>
            Dados e imagens de jogos são fornecidos por serviços da Steam. O
            WeXP não é afiliado à Valve Corporation.
          </p>
        </div>
      </div>

      <div className={styles.bottom}>
        <span>© {new Date().getFullYear()} WeXP</span>
        <span>Projeto desenvolvido para fins acadêmicos.</span>
      </div>
    </footer>
  );
}

export default Footer;
