import { useNavigate } from 'react-router';
import { CaretRightIcon, LockSimpleIcon } from '@phosphor-icons/react';
import type { Achievement } from '../../types/Achievement';
import styles from './styles.module.css';
import SaveButton from '../SaveButton';

type AchievementCardProps = {
  achievement: Achievement;
  showSecret: boolean;
  gameId: string;
  style?: React.CSSProperties;
};

function AchievementCard({
  achievement,
  showSecret,
  gameId,
  style,
}: AchievementCardProps) {
  const navigate = useNavigate();

  const isHidden = achievement.hidden && !showSecret;

  function handleClick() {
    navigate(`/games/${gameId}/achievements/${achievement.id}/guide`);
  }

  const cardProps = {
    onClick: handleClick,
    role: 'link',
    tabIndex: 0,
    style,
    onKeyDown: (event: React.KeyboardEvent) => {
      if (event.target === event.currentTarget && event.key === 'Enter') {
        handleClick();
      }
    },
  };

  const arrow = (
    <span className={styles.arrow} aria-hidden='true'>
      <CaretRightIcon size={18} weight='bold' />
    </span>
  );

  // Se a conquista NÃO for secreta desde o início, usamos a renderização padrão
  if (!achievement.hidden) {
    return (
      <div className={styles.achievementCard} {...cardProps}>
        <div className={styles.iconContainer}>
          <img
            className={styles.achievementIcon}
            src={achievement.icon}
            alt=''
            loading='lazy'
          />
        </div>

        <div className={styles.achievementInfo}>
          <h3>{achievement.name}</h3>
          {achievement.description && <p>{achievement.description}</p>}
        </div>

        <SaveButton type='ACHIEVEMENT' resourceId={achievement.id} compact />
        {arrow}
      </div>
    );
  }

  // Para conquistas secretas, mantemos AMBOS os elementos renderizados no DOM
  return (
    <div
      className={`${styles.achievementCard} ${styles.secret} ${
        isHidden ? styles.hidden : ''
      } ${showSecret ? styles.revealed : ''}`}
      aria-label={isHidden ? 'Conquista secreta' : achievement.name}
      {...cardProps}
    >
      <div className={styles.iconContainer}>
        <img
          className={styles.revealedIcon}
          src={achievement.icon}
          alt=''
          loading='lazy'
        />
        <span className={styles.hiddenIcon}>
          <LockSimpleIcon size={22} weight='fill' />
        </span>
      </div>

      <div className={styles.achievementInfo}>
        <div className={styles.hiddenInfo}>
          <h3>Conquista secreta</h3>
          <p>Passe o mouse para revelar os detalhes</p>
        </div>

        <div className={styles.revealedInfo}>
          <h3>
            {achievement.name}
            <span className={styles.secretBadge}>Secreta</span>
          </h3>
          {achievement.description && <p>{achievement.description}</p>}
        </div>
      </div>

      <SaveButton type='ACHIEVEMENT' resourceId={achievement.id} compact />
      {arrow}
    </div>
  );
}
export default AchievementCard;
