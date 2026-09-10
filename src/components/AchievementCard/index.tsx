import { useNavigate } from 'react-router';
import type { Achievement } from '../../types/Achievement';
import styles from './styles.module.css';

type AchievementCardProps = {
  achievement: Achievement;
  showSecret: boolean;
  gameId: string;
};

function AchievementCard({
  achievement,
  showSecret,
  gameId,
}: AchievementCardProps) {
  const navigate = useNavigate();

  const isHidden = achievement.hidden && !showSecret;

  function handleClick() {
    navigate(`/games/${gameId}/achievements/${achievement.id}/guide`);
  }

  // Se a conquista NÃO for secreta desde o início, usamos a renderização padrão
  if (!achievement.hidden) {
    return (
      <div className={styles.achievementCard} onClick={handleClick}>
        <div className={styles.iconContainer}>
          <img
            className={styles.achievementIcon}
            src={achievement.icon}
            alt={achievement.name}
          />
        </div>

        <div className={styles.achievementInfo}>
          <h3>{achievement.name}</h3>
          {achievement.description && <p>{achievement.description}</p>}
        </div>
      </div>
    );
  }

  // Para conquistas secretas, mantemos AMBOS os elementos renderizados no DOM
  return (
    <div
      className={`${styles.achievementCard} ${
        isHidden ? styles.hidden : ''
      } ${showSecret ? styles.revealed : ''}`}
      onClick={handleClick}
    >
      <div className={styles.iconContainer}>
        <span className={styles.hiddenIcon}>?</span>
        <img
          className={styles.revealedIcon}
          src={achievement.icon}
          alt={achievement.name}
        />
      </div>

      <div className={styles.achievementInfo}>
        <div className={styles.hiddenInfo}>
          <h3>Conquista secreta</h3>
          <p>Passe o mouse em cima para revelar detalhes</p>
        </div>

        <div className={styles.revealedInfo}>
          <h3>{achievement.name}</h3>
          {achievement.description && <p>{achievement.description}</p>}
        </div>
      </div>
    </div>
  );
}
export default AchievementCard;
