import type { Achievement } from '../../mocks/gameAchievements';
import styles from './styles.module.css';

type AchievementCardProps = {
  achievement: Achievement;
};

function AchievementCard({ achievement }: AchievementCardProps) {
  const isHidden = achievement.is_hidden;

  return (
    <div
      className={`${styles.achievementCard} ${isHidden ? styles.hidden : ''}`}
    >
      <div className={styles.iconContainer}>
        {isHidden ? (
          <>
            <span className={styles.hiddenIcon}>?</span>

            <img
              className={styles.revealedIcon}
              src={achievement.icon_url}
              alt={achievement.name}
            />
          </>
        ) : (
          <img
            className={styles.achievementIcon}
            src={achievement.icon_url}
            alt={achievement.name}
          />
        )}
      </div>

      <div className={styles.achievementInfo}>
        {isHidden ? (
          <>
            <h3 className={styles.hiddenTitle}>Conquista secreta</h3>

            <p className={styles.hiddenDescription}>
              Passe o mouse em cima para revelar detalhes
            </p>

            <div className={styles.revealedInfo}>
              <h3>{achievement.name}</h3>

              {achievement.description && <p>{achievement.description}</p>}
            </div>
          </>
        ) : (
          <>
            <h3>{achievement.name}</h3>

            {achievement.description && <p>{achievement.description}</p>}
          </>
        )}
      </div>
    </div>
  );
}

export default AchievementCard;
