import {
  ClockCounterClockwiseIcon,
  NotePencilIcon,
} from '@phosphor-icons/react';
import GuideContent from '../../components/GuideContent';
import type { Achievement } from '../../types/Achievement';
import styles from './styles.module.css';

type AchievementPageProps = {
  achievement: Achievement;
  markdown: string;
};

function AchievementPage({ achievement, markdown }: AchievementPageProps) {
  return (
    <div className={styles.achievementPage}>
      <div className={styles.achievementPanel}>
        <div className={styles.achievementInfo}>
          <img
            src={achievement.icon_url}
            alt={`Ícone da conquista ${achievement.name}`}
          />

          <div className={styles.achievementDetails}>
            <p>{achievement.name}</p>

            {achievement.description && <p>{achievement.description}</p>}
          </div>
        </div>

        <div className={styles.achievementPanelButtons}>
          <button
            type='button'
            title='Ver histórico de alterações'
            aria-label='Ver histórico alterações'
          >
            <ClockCounterClockwiseIcon />
          </button>

          <button type='button' title='Editar guia' aria-label='Editar guia'>
            <NotePencilIcon />
          </button>
        </div>
      </div>
      <GuideContent markdown={markdown} />
    </div>
  );
}

export default AchievementPage;
