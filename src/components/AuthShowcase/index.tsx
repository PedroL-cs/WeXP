import {
  BookOpenTextIcon,
  CrownSimpleIcon,
  HandshakeIcon,
  TrophyIcon,
} from '@phosphor-icons/react';
import styles from './styles.module.css';

type AuthShowcaseProps = {
  title: string;
  description: string;
};

// Conquistas ilustrativas da própria comunidade WeXP
const toasts = [
  { icon: BookOpenTextIcon, name: 'Primeiro guia publicado', xp: 50 },
  { icon: HandshakeIcon, name: 'Revisão aprovada', xp: 25 },
  { icon: CrownSimpleIcon, name: 'Platina desbloqueada', xp: 100 },
  { icon: TrophyIcon, name: 'Ajudou 100 jogadores', xp: 250 },
];

function AuthShowcase({ title, description }: AuthShowcaseProps) {
  return (
    <section className={styles.showcase}>
      <div className={styles.stack} aria-hidden='true'>
        {toasts.map(({ icon: Icon, name, xp }, index) => (
          <div
            key={name}
            className={styles.toast}
            style={{ '--i': index } as React.CSSProperties}
          >
            <span className={styles.toastIcon}>
              <Icon size={24} weight='fill' />
            </span>
            <span className={styles.toastText}>
              <small>Conquista desbloqueada</small>
              <strong>{name}</strong>
            </span>
            <span className={styles.toastXp}>+{xp} XP</span>
          </div>
        ))}
      </div>

      <div className={styles.overlay}>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
    </section>
  );
}

export default AuthShowcase;
