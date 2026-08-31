import { Link } from 'react-router';
import styles from './styles.module.css';

function NotFound() {
  return (
    <div className={styles.notFound}>
      <h1>404</h1>
      <p>...Página não encontrada!</p>
      <p>A página que você está procurando não existe ou foi removida.</p>
      <p>
        Retornar para a <Link to='/'>Home</Link>
      </p>
    </div>
  );
}

export default NotFound;
