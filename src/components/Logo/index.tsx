import styles from './styles.module.css';

type LogoProps = {
  onClick?: () => void;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
};

function Logo({ onClick, size = 'md', className = '' }: LogoProps) {
  const sizeClass = styles[`size-${size}`] || '';

  return (
    <div
      className={`${styles['logo-container']} ${sizeClass} ${className}`.trim()}
      onClick={onClick}
    >
      <div className={styles['wasd-icon']} aria-label='Ícone WASD'>
        <div className={`${styles.key} ${styles['key-w']}`}></div>
        <div className={`${styles.key} ${styles['key-a']}`}></div>
        <div className={`${styles.key} ${styles['key-s']}`}></div>
        <div className={`${styles.key} ${styles['key-d']}`}></div>
      </div>
      <h1 className={styles.logo}>WeXP</h1>
    </div>
  );
}

export default Logo;
