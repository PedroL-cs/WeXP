package com.wexp;

import com.wexp.feature.achievement.*;
import com.wexp.feature.game.*;
import com.wexp.feature.guide.*;
import com.wexp.feature.guide.dto.*;
import com.wexp.feature.revision.*;
import com.wexp.feature.revision.dto.*;
import com.wexp.feature.user.*;
import com.wexp.shared.exception.*;
import com.wexp.shared.persistence.PersistenceTransactions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"}, showSql = false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({GuideService.class, GuideRevisionService.class, GuideRevisionVoteService.class,
        GuideRevisionLifecycleService.class, PersistenceTransactions.class})
class RevisionVotingIntegrationTests {
    @Autowired private GuideService guideService;
    @Autowired private GuideRevisionService revisionService;
    @Autowired private GuideRevisionVoteService voteService;
    @Autowired private GuideRevisionLifecycleService lifecycle;
    @Autowired private IGuideRepository guides;
    @Autowired private IGuideRevisionRepository revisions;
    @Autowired private IGuideRevisionVoteRepository votes;
    @Autowired private IUserRepository users;
    @Autowired private IGameRepository games;
    @Autowired private IAchievementRepository achievements;
    @Autowired private PlatformTransactionManager transactionManager;

    private UserEntity creator;
    private UserEntity editor;
    private UserEntity voter;
    private UserEntity secondVoter;
    private AchievementEntity achievement;
    private GuideResponseDto guide;

    @BeforeEach
    void setup() {
        creator = user();
        editor = user();
        voter = user();
        secondVoter = user();
        achievement = achievement();
        guide = guideService.createInitialGuide(achievement.getPublicId(),
                new CreateGuideRequestDto("Conteúdo original preservado"), creator);
    }

    @Test
    void shouldCreateOriginalVersionAndKeepStableGuideId() {
        assertNotNull(guide.originalRevisionId());
        assertEquals(guide.originalRevisionId(), guide.activeRevisionId());
        var original = revisionService.getRevision(guide.originalRevisionId(), null);
        assertTrue(original.isOriginal());
        assertTrue(original.isMain());
        assertNull(original.getBaseRevisionId());
        assertEquals(0, original.getVoteCount());
        assertEquals(RevisionStatus.PUBLISHED, original.getStatus());
    }

    @Test
    void shouldPromoteRevisionWithoutChangingOriginalContentOrCreator() {
        var revision = revision(guide.originalRevisionId(), editor, "Conteúdo corrigido pelo editor");
        RevisionVoteResponse response = voteService.upvote(revision.getPublicId(), voter);
        GuideResponseDto current = guideService.getGuideByAchievementPublicId(achievement.getPublicId());

        assertTrue(response.isMain());
        assertEquals(1, response.voteCount());
        assertEquals(guide.id(), current.id());
        assertEquals(revision.getPublicId(), current.activeRevisionId());
        assertEquals("Conteúdo corrigido pelo editor", current.content());
        assertEquals(editor.getPublicId(), current.authorPublicId());
        assertEquals(creator.getPublicId(), current.creatorPublicId());
        assertEquals(2, current.version());
        assertEquals("Conteúdo original preservado", guides.findByPublicId(guide.id()).orElseThrow().getContent());
        assertEquals("Conteúdo original preservado", revisionService.getRevision(guide.originalRevisionId(), null).getContent());
    }

    @Test
    void shouldAllowOriginalVersionToWinAgain() {
        var revision = revision(guide.originalRevisionId(), editor, "Conteúdo da primeira alternativa");
        voteService.upvote(revision.getPublicId(), voter);
        voteService.upvote(guide.originalRevisionId(), voter);
        assertEquals(revision.getPublicId(), current().activeRevisionId());

        voteService.upvote(guide.originalRevisionId(), secondVoter);

        assertEquals(guide.originalRevisionId(), current().activeRevisionId());
        assertEquals(creator.getPublicId(), current().authorPublicId());
        assertEquals(3, current().version());
    }

    @Test
    void shouldMakeUpvoteAndRemovalIdempotent() {
        var revision = revision(guide.originalRevisionId(), editor, "Conteúdo alternativo para avaliar");
        voteService.upvote(revision.getPublicId(), voter);
        RevisionVoteResponse duplicate = voteService.upvote(revision.getPublicId(), voter);
        assertEquals(1, duplicate.voteCount());
        assertTrue(duplicate.viewerHasVoted());
        assertEquals(2, duplicate.guideVersion());

        voteService.removeUpvote(revision.getPublicId(), voter);
        RevisionVoteResponse removedAgain = voteService.removeUpvote(revision.getPublicId(), voter);
        assertEquals(0, removedAgain.voteCount());
        assertFalse(removedAgain.viewerHasVoted());
        // With every version at zero, the current main retains the tie.
        assertEquals(revision.getPublicId(), current().activeRevisionId());
        assertEquals(2, current().version());
    }

    @Test
    void shouldPermitOneUserToSupportMultipleVersions() {
        var first = revision(guide.originalRevisionId(), editor, "Primeira proposta de conteúdo");
        var second = revision(guide.originalRevisionId(), creator, "Segunda proposta de conteúdo");
        voteService.upvote(first.getPublicId(), voter);
        voteService.upvote(second.getPublicId(), voter);
        voteService.upvote(guide.originalRevisionId(), voter);

        assertEquals(1, revisionService.getRevision(first.getPublicId(), voter).getVoteCount());
        assertEquals(1, revisionService.getRevision(second.getPublicId(), voter).getVoteCount());
        assertTrue(revisionService.getRevision(guide.originalRevisionId(), voter).isViewerHasVoted());
    }

    @Test
    void shouldRejectSelfVotesIncludingOriginalAuthor() {
        var revision = revision(guide.originalRevisionId(), editor, "Proposta escrita pelo editor");
        assertError(ExceptionResponse.OwnRevisionVote, () -> voteService.upvote(revision.getPublicId(), editor));
        assertError(ExceptionResponse.OwnRevisionVote, () -> voteService.upvote(guide.originalRevisionId(), creator));
        assertEquals(0, revisionService.getRevision(revision.getPublicId(), voter).getVoteCount());
        assertEquals(guide.originalRevisionId(), current().activeRevisionId());
    }

    @Test
    void shouldKeepCurrentMainOnTie() {
        var older = revision(guide.originalRevisionId(), editor, "Proposta mais antiga publicada");
        var newer = revision(guide.originalRevisionId(), creator, "Proposta mais nova publicada");
        voteService.upvote(newer.getPublicId(), voter);
        voteService.upvote(older.getPublicId(), voter);

        assertEquals(newer.getPublicId(), current().activeRevisionId());
        assertEquals(2, current().version());
    }

    @Test
    void shouldChooseOldestTopCandidateWhenIncumbentLosesLead() {
        var older = revision(guide.originalRevisionId(), editor, "Primeira alternativa da comunidade");
        var newer = revision(guide.originalRevisionId(), creator, "Segunda alternativa da comunidade");
        var leader = revision(guide.originalRevisionId(), editor, "Terceira alternativa da comunidade");
        voteService.upvote(leader.getPublicId(), voter);
        voteService.upvote(leader.getPublicId(), secondVoter);
        voteService.upvote(older.getPublicId(), voter);
        voteService.upvote(newer.getPublicId(), voter);
        voteService.removeUpvote(leader.getPublicId(), voter);
        assertEquals(leader.getPublicId(), current().activeRevisionId());

        voteService.removeUpvote(leader.getPublicId(), secondVoter);

        assertEquals(older.getPublicId(), current().activeRevisionId());
    }

    @Test
    void shouldUseWinningRevisionAsBaseForNewEdits() {
        var winner = revision(guide.originalRevisionId(), editor, "Versão que venceu a votação");
        voteService.upvote(winner.getPublicId(), voter);
        var child = revision(current().activeRevisionId(), creator, "Edição baseada na vencedora");

        assertEquals(winner.getPublicId(), child.getBaseRevisionId());
        assertTrue(child.isBasedOnCurrentMain());
        assertEquals(0, child.getVoteCount());
        assertFalse(child.isMain());
        assertEquals(winner.getPublicId(), current().activeRevisionId());
    }

    @Test
    void shouldPreserveStaleBaseWithoutTransferringVotesOrRebasing() {
        String editingBase = current().activeRevisionId();
        var winner = revision(editingBase, editor, "Conteúdo vencedor antes da publicação");
        voteService.upvote(winner.getPublicId(), voter);

        var published = revision(editingBase, creator, "Edição iniciada na versão anterior");

        assertEquals(editingBase, published.getBaseRevisionId());
        assertFalse(published.isBasedOnCurrentMain());
        assertEquals(0, published.getVoteCount());
        assertEquals(winner.getPublicId(), current().activeRevisionId());
    }

    @Test
    void shouldRejectBaseFromAnotherGuide() {
        var other = guideService.createInitialGuide(achievement().getPublicId(),
                new CreateGuideRequestDto("Conteúdo original de outra conquista"), creator);
        assertError(ExceptionResponse.InvalidRevisionBase,
                () -> revision(other.originalRevisionId(), editor, "Tentativa com uma base incorreta"));
    }

    @Test
    void shouldKeepPublishedContentsAndAuthorshipImmutable() {
        var response = revision(guide.originalRevisionId(), editor, "Conteúdo que receberá os votos");
        voteService.upvote(response.getPublicId(), voter);
        GuideRevisionEntity stored = revisions.findByPublicId(response.getPublicId()).orElseThrow();
        stored.setContent("Alteração depois da publicação");
        stored.setAuthor(creator);
        stored.setChangeSummary("Outro resumo");
        revisions.saveAndFlush(stored);

        var unchanged = revisionService.getRevision(response.getPublicId(), voter);
        assertEquals("Conteúdo que receberá os votos", unchanged.getContent());
        assertEquals(editor.getPublicId(), unchanged.getAuthorPublicId());
        assertEquals("Resumo da alteração", unchanged.getChangeSummary());
        assertEquals(1, unchanged.getVoteCount());
    }

    @Test
    void shouldPageByVotesAndReturnViewerStateAndMainFlags() {
        var first = revision(guide.originalRevisionId(), editor, "Primeira proposta para a listagem");
        var second = revision(guide.originalRevisionId(), creator, "Segunda proposta para a listagem");
        voteService.upvote(second.getPublicId(), voter);
        voteService.upvote(second.getPublicId(), secondVoter);
        voteService.upvote(first.getPublicId(), voter);

        var page = revisionService.getRevisionsByAchievement(achievement.getPublicId(), PageRequest.of(0, 2), voter);
        assertEquals(3, page.getTotalElements());
        assertEquals(second.getPublicId(), page.getContent().get(0).getPublicId());
        assertEquals(2, page.getContent().get(0).getVoteCount());
        assertTrue(page.getContent().get(0).isMain());
        assertTrue(page.getContent().get(0).isViewerHasVoted());
        assertFalse(revisionService.getRevision(second.getPublicId(), null).isViewerHasVoted());
        var last = revisionService.getRevisions(guide.id(), PageRequest.of(1, 2), voter);
        assertEquals(guide.originalRevisionId(), last.getContent().get(0).getPublicId());
        assertTrue(last.getContent().get(0).isOriginal());
    }

    @Test
    void shouldRejectVotingOrBasingEditsOnUnavailableRevisions() {
        var response = revision(guide.originalRevisionId(), editor, "Conteúdo não disponível para votação");
        GuideRevisionEntity unavailable = revisions.findByPublicId(response.getPublicId()).orElseThrow();
        unavailable.setStatus(RevisionStatus.REJECTED);
        revisions.saveAndFlush(unavailable);

        assertError(ExceptionResponse.RevisionNotEligible, () -> voteService.upvote(response.getPublicId(), voter));
        assertError(ExceptionResponse.InvalidRevisionBase, () -> revision(response.getPublicId(), creator, "Nova edição de revisão indisponível"));
        assertError(ExceptionResponse.RevisionNotFound, () -> revisionService.getRevision(response.getPublicId(), null));
        assertEquals(1, revisionService.getRevisions(guide.id(), PageRequest.of(0, 20), null).getTotalElements());
    }

    @Test
    void shouldRollbackVoteAndMainSelectionTogether() {
        var revision = revision(guide.originalRevisionId(), editor, "Conteúdo candidato ao guia principal");
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertTrue(voteService.upvote(revision.getPublicId(), voter).isMain());
            status.setRollbackOnly();
        });

        assertEquals(guide.originalRevisionId(), current().activeRevisionId());
        assertEquals(1, current().version());
        assertEquals(0, revisionService.getRevision(revision.getPublicId(), voter).getVoteCount());
    }

    @Test
    void shouldHandleDuplicateConcurrentRequestsWithoutDuplicatingVotes() throws Exception {
        var revision = revision(guide.originalRevisionId(), editor, "Conteúdo para requisições simultâneas");
        var results = concurrently(() -> voteService.upvote(revision.getPublicId(), voter),
                () -> voteService.upvote(revision.getPublicId(), voter));

        assertEquals(1, results.get(0).voteCount());
        assertEquals(1, results.get(1).voteCount());
        assertEquals(2, current().version());
        assertEquals(1, revisionService.getRevision(revision.getPublicId(), voter).getVoteCount());
    }

    @Test
    void shouldSerializeConcurrentVotesForDifferentCandidates() throws Exception {
        var first = revision(guide.originalRevisionId(), editor, "Primeira candidata simultânea");
        var second = revision(guide.originalRevisionId(), creator, "Segunda candidata simultânea");
        var results = concurrently(() -> voteService.upvote(first.getPublicId(), voter),
                () -> voteService.upvote(second.getPublicId(), secondVoter));

        assertEquals(results.get(0).activeRevisionId(), results.get(1).activeRevisionId());
        assertEquals(results.get(0).activeRevisionId(), current().activeRevisionId());
        assertEquals(2, current().version());
        String challenger = first.getPublicId().equals(current().activeRevisionId()) ? second.getPublicId() : first.getPublicId();
        voteService.upvote(challenger, user());
        assertEquals(challenger, current().activeRevisionId());
    }

    @Test
    void shouldEnforceVoteUniquenessInDatabase() {
        GuideRevisionEntity original = revisions.findByPublicId(guide.originalRevisionId()).orElseThrow();
        votes.saveAndFlush(GuideRevisionVoteEntity.builder().revision(original).user(voter).build());

        assertThrows(DataIntegrityViolationException.class,
                () -> votes.saveAndFlush(GuideRevisionVoteEntity.builder().revision(original).user(voter).build()));
        assertEquals(1, votes.countByRevision_Id(original.getId()));
    }

    @Test
    void shouldSerializeConcurrentVotingAndRemoval() throws Exception {
        var revision = revision(guide.originalRevisionId(), editor, "Conteúdo com voto e retirada simultâneos");
        voteService.upvote(guide.originalRevisionId(), voter);
        var results = concurrently(() -> voteService.upvote(revision.getPublicId(), voter),
                () -> voteService.removeUpvote(guide.originalRevisionId(), voter));

        assertEquals(revision.getPublicId(), current().activeRevisionId());
        assertEquals(1, revisionService.getRevision(revision.getPublicId(), voter).getVoteCount());
        assertEquals(0, revisionService.getRevision(guide.originalRevisionId(), voter).getVoteCount());
        assertEquals(2, current().version());
    }

    @Test
    void shouldInitializeOnlyOneOriginalDuringConcurrentMigration() throws Exception {
        GuideEntity legacy = guides.saveAndFlush(GuideEntity.builder().achievement(achievement()).author(creator)
                .content("Guia legado inicializado em paralelo").build());
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> lifecycle.migrateGuide(legacy.getPublicId()));
            var second = executor.submit(() -> lifecycle.migrateGuide(legacy.getPublicId()));
            first.get(20, TimeUnit.SECONDS);
            second.get(20, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
        var migrated = guideService.getGuideByPublicId(legacy.getPublicId());
        assertEquals(migrated.originalRevisionId(), migrated.activeRevisionId());
        assertEquals(1, revisionService.getRevisions(legacy.getPublicId(), PageRequest.of(0, 20), null).getTotalElements());
    }

    @Test
    void shouldMigrateLegacyDataOnceWithoutLosingContentOrAuthorship() {
        GuideEntity legacy = guides.saveAndFlush(GuideEntity.builder().achievement(achievement()).author(creator)
                .content("Conteúdo original do guia legado").build());
        GuideRevisionEntity old = revisions.saveAndFlush(GuideRevisionEntity.builder().guide(legacy).author(editor)
                .content("Conteúdo de uma revisão antiga").changeSummary("Resumo antigo").status(RevisionStatus.PENDING).build());
        var migration = new GuideRevisionMigration(guides, lifecycle);

        migration.run(null);
        migration.run(null);

        var migrated = guideService.getGuideByPublicId(legacy.getPublicId());
        assertEquals(migrated.originalRevisionId(), migrated.activeRevisionId());
        assertEquals("Conteúdo original do guia legado", migrated.content());
        var preserved = revisionService.getRevision(old.getPublicId(), null);
        assertEquals(RevisionStatus.PUBLISHED, preserved.getStatus());
        assertEquals(editor.getPublicId(), preserved.getAuthorPublicId());
        assertEquals("Conteúdo de uma revisão antiga", preserved.getContent());
        assertEquals(migrated.originalRevisionId(), preserved.getBaseRevisionId());
        assertEquals(2, revisionService.getRevisions(legacy.getPublicId(), PageRequest.of(0, 20), null).getTotalElements());
    }

    @Test
    void shouldRequireAuthenticationAndHandleUnknownResources() {
        assertError(ExceptionResponse.Unauthorized, () -> voteService.upvote(guide.originalRevisionId(), null));
        assertError(ExceptionResponse.Unauthorized, () -> voteService.removeUpvote(guide.originalRevisionId(), null));
        assertError(ExceptionResponse.RevisionNotFound, () -> voteService.upvote("missing", voter));
        assertError(ExceptionResponse.AchievementNotFound,
                () -> revisionService.getRevisionsByAchievement("missing", PageRequest.of(0, 20), null));
    }

    private GuideRevisionResponse revision(String base, UserEntity author, String content) {
        CreateGuideRevisionRequest request = new CreateGuideRevisionRequest();
        request.setBaseRevisionId(base);
        request.setContent(content);
        request.setChangeSummary("Resumo da alteração");
        return revisionService.createRevision(guide.id(), request, author);
    }

    private GuideResponseDto current() {
        return guideService.getGuideByPublicId(guide.id());
    }

    private UserEntity user() {
        String login = "u" + UUID.randomUUID().toString().substring(0, 20);
        return users.saveAndFlush(UserEntity.builder().login(login).email(login + "@example.com")
                .username("Usuário de teste").passwordHash("encoded").birthDate(LocalDate.of(1990, 1, 1)).build());
    }

    private AchievementEntity achievement() {
        GameEntity game = games.saveAndFlush(GameEntity.builder().name("Jogo de teste").build());
        return achievements.saveAndFlush(AchievementEntity.builder().name("Conquista de teste").isHidden(false).game(game).build());
    }

    private void assertError(ExceptionResponse expected, Runnable operation) {
        assertEquals(expected, assertThrows(ApiException.class, operation::run).getExceptionResponse());
    }

    private List<RevisionVoteResponse> concurrently(Callable<RevisionVoteResponse> first, Callable<RevisionVoteResponse> second) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        java.util.function.Function<Callable<RevisionVoteResponse>, Callable<RevisionVoteResponse>> task = operation -> () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Timed out waiting for concurrent start");
            return operation.call();
        };
        try {
            var firstResult = executor.submit(task.apply(first));
            var secondResult = executor.submit(task.apply(second));
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            return List.of(firstResult.get(20, TimeUnit.SECONDS), secondResult.get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
