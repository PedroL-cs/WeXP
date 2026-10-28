import {
  ArrowsClockwiseIcon,
  BookOpenTextIcon,
  GameControllerIcon,
  MedalIcon,
  UsersThreeIcon,
} from '@phosphor-icons/react';

import styles from './styles.module.css';

const pillars = [
  {
    icon: GameControllerIcon,
    number: '01',
    title: 'Descobrir',
    description:
      'Encontre jogos e explore informações, categorias, gêneros e conquistas reunidas a partir da Steam.',
  },
  {
    icon: BookOpenTextIcon,
    number: '02',
    title: 'Compartilhar',
    description:
      'Transforme experiência em guias claros para ajudar outros jogadores a superar desafios e completar conquistas.',
  },
  {
    icon: ArrowsClockwiseIcon,
    number: '03',
    title: 'Evoluir',
    description:
      'Sugira revisões, compare versões e vote nas contribuições que melhoram o conhecimento coletivo.',
  },
];

function AboutPage() {
  return (
    <main className={styles.aboutPage}>
      <section className={styles.hero}>
        <div className={styles.heroCopy}>
          <span className={styles.eyebrow}>Sobre o WeXP</span>
          <h1>
            Experiência de jogador,
            <span> conhecimento de comunidade.</span>
          </h1>
          <p>
            O WeXP é uma plataforma colaborativa criada para aproximar jogos,
            conquistas e pessoas. Um espaço onde descobrir um desafio é apenas o
            começo e cada solução pode evoluir com a comunidade.
          </p>
        </div>

        <div className={styles.heroMark} aria-hidden='true'>
          <MedalIcon weight='fill' />
        </div>
      </section>

      <section className={styles.pillars} aria-labelledby='proposal-title'>
        <div className={styles.sectionIntro}>
          <span>Como funciona</span>
          <h2 id='proposal-title'>Uma jornada construída em conjunto</h2>
        </div>

        <div className={styles.pillarGrid}>
          {pillars.map(({ icon: Icon, number, title, description }) => (
            <article className={styles.pillar} key={title}>
              <div className={styles.pillarTop}>
                <Icon size={28} weight='duotone' />
                <span>{number}</span>
              </div>
              <h3>{title}</h3>
              <p>{description}</p>
            </article>
          ))}
        </div>
      </section>

      <section className={styles.collaboration}>
        <div className={styles.collaborationIcon}>
          <UsersThreeIcon size={38} weight='duotone' />
        </div>
        <div>
          <span className={styles.eyebrow}>Conhecimento vivo</span>
          <h2>Guias que não param na primeira versão</h2>
          <p>
            No WeXP, um guia pode receber novas propostas sem apagar sua
            história. As revisões preservam a versão de origem, permitem
            comparação e usam a participação da comunidade para destacar o
            conteúdo mais útil.
          </p>
        </div>
      </section>

      <section className={styles.project}>
        <div className={styles.projectCopy}>
          <span className={styles.eyebrow}>O projeto</span>
          <h2>Tecnologia aplicada a uma experiência real</h2>
          <p>
            Desenvolvido para fins acadêmicos, o projeto integra uma aplicação
            web responsiva a serviços externos e recursos colaborativos. Sua
            arquitetura separa interface, API e persistência para facilitar a
            manutenção e a evolução da plataforma.
          </p>
        </div>

        <div className={styles.stack} aria-label='Tecnologias utilizadas'>
          <span>React</span>
          <span>TypeScript</span>
          <span>Spring Boot</span>
          <span>PostgreSQL</span>
          <span>Supabase</span>
          <span>Steam API</span>
        </div>
      </section>
    </main>
  );
}

export default AboutPage;
