package com.wexp.feature.library;

import com.wexp.feature.achievement.IAchievementRepository;
import com.wexp.feature.game.IGameRepository;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.feature.library.dto.SavedItemResponse;
import com.wexp.feature.library.dto.SavedStatusResponse;
import com.wexp.feature.user.UserEntity;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import com.wexp.shared.persistence.PersistenceTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SavedItemService {

    private final SavedItemRepository savedItems;
    private final IGameRepository games;
    private final IAchievementRepository achievements;
    private final IGuideRepository guides;
    private final PersistenceTransactions transactions;

    public SavedStatusResponse save(
            SavedResourceType type,
            String resourceId,
            UserEntity currentUser
    ) {
        requireUser(currentUser);
        Optional<SavedItemEntity> existing = findSavedItem(type, resourceId, currentUser.getId());
        if (existing.isPresent()) {
            return SavedStatusResponse.saved(existing.get());
        }

        SavedItemEntity item = newItem(type, resourceId, currentUser);
        try {
            SavedItemEntity saved = transactions.insert(() -> savedItems.saveAndFlush(item));
            return SavedStatusResponse.saved(saved);
        } catch (DataIntegrityViolationException exception) {
            // A constraint única torna PUT idempotente também entre instâncias concorrentes.
            return findSavedItem(type, resourceId, currentUser.getId())
                    .map(SavedStatusResponse::saved)
                    .orElseThrow(() -> exception);
        }
    }

    @Transactional
    public void remove(
            SavedResourceType type,
            String resourceId,
            UserEntity currentUser
    ) {
        requireUser(currentUser);
        findSavedItem(type, resourceId, currentUser.getId()).ifPresent(savedItems::delete);
    }

    @Transactional(readOnly = true)
    public SavedStatusResponse status(
            SavedResourceType type,
            String resourceId,
            UserEntity currentUser
    ) {
        requireUser(currentUser);
        Optional<SavedItemEntity> item = findSavedItem(type, resourceId, currentUser.getId());
        if (item.isPresent()) {
            return SavedStatusResponse.saved(item.get());
        }
        requireResource(type, resourceId);
        return SavedStatusResponse.notSaved(type, resourceId);
    }

    @Transactional(readOnly = true)
    public Page<SavedItemResponse> list(
            SavedResourceType type,
            Pageable pageable,
            UserEntity currentUser
    ) {
        requireUser(currentUser);
        Page<SavedItemEntity> page = type == null
                ? savedItems.findByUser_Id(currentUser.getId(), pageable)
                : savedItems.findByUser_IdAndResourceType(currentUser.getId(), type, pageable);
        return page.map(SavedItemResponse::new);
    }

    private SavedItemEntity newItem(
            SavedResourceType type,
            String resourceId,
            UserEntity currentUser
    ) {
        return switch (type) {
            case GAME -> SavedItemEntity.game(
                    currentUser,
                    games.findByPublicId(resourceId)
                            .orElseThrow(() -> new ApiException(ExceptionResponse.GameNotFound))
            );
            case ACHIEVEMENT -> SavedItemEntity.achievement(
                    currentUser,
                    achievements.findByPublicId(resourceId)
                            .orElseThrow(() -> new ApiException(ExceptionResponse.AchievementNotFound))
            );
            case GUIDE -> SavedItemEntity.guide(
                    currentUser,
                    guides.findByPublicId(resourceId)
                            .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound))
            );
        };
    }

    private void requireResource(SavedResourceType type, String resourceId) {
        boolean exists = switch (type) {
            case GAME -> games.findByPublicId(resourceId).isPresent();
            case ACHIEVEMENT -> achievements.existsByPublicId(resourceId);
            case GUIDE -> guides.existsByPublicId(resourceId);
        };
        if (!exists) {
            throw new ApiException(switch (type) {
                case GAME -> ExceptionResponse.GameNotFound;
                case ACHIEVEMENT -> ExceptionResponse.AchievementNotFound;
                case GUIDE -> ExceptionResponse.GuideNotFound;
            });
        }
    }

    private Optional<SavedItemEntity> findSavedItem(
            SavedResourceType type,
            String resourceId,
            Long userId
    ) {
        return switch (type) {
            case GAME -> savedItems.findByUser_IdAndGame_PublicId(userId, resourceId);
            case ACHIEVEMENT -> savedItems.findByUser_IdAndAchievement_PublicId(userId, resourceId);
            case GUIDE -> savedItems.findByUser_IdAndGuide_PublicId(userId, resourceId);
        };
    }

    private void requireUser(UserEntity currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new ApiException(ExceptionResponse.Unauthorized);
        }
    }
}
