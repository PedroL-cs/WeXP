import { Link } from 'react-router';
import { HouseIcon, MapTrifoldIcon } from '@phosphor-icons/react';
import styles from './styles.module.css';

function NotFound() {
  return (
    <div className={styles.notFound}>
      <h1 className={styles.code} data-text='404'>
        404
      </h1>

      {/* Achar uma página inexistente também é uma conquista */}
      <div className={styles.toast} role='status'>
        <span className={styles.toastIcon}>
          <MapTrifoldIcon size={28} weight='fill' />
        </span>
        <span className={styles.toastText}>
          <small>Conquista secreta desbloqueada</small>
          <strong>Perdido no mapa</strong>
          <span>Encontre uma página que não existe.</span>
        </span>
        <span className={styles.toastXp}>+404 XP</span>
      </div>

      <p className={styles.message}>
        A página que você está procurando não existe ou foi removida.
      </p>

      <Link to='/' className={styles.home}>
        <HouseIcon size={18} weight='fill' />
        Voltar para a Home
      </Link>
    </div>
  );
}

export default NotFound;
