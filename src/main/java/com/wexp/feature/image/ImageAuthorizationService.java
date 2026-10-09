package com.wexp.feature.image;

import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.feature.user.UserEntity;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageAuthorizationService {

    private final IGuideRepository guideRepository;

    @Transactional(readOnly = true)
    public void authorizeModification(String ownerType, String ownerId, String variant, UserEntity currentUser) {
        if (currentUser == null || currentUser.getPublicId() == null) {
            throw new ApiException(ExceptionResponse.Unauthorized);
        }

        if ("user".equals(ownerType)
                && "profile".equals(variant)
                && currentUser.getPublicId().equals(ownerId)) {
            return;
        }

        if ("guide".equals(ownerType)) {
            GuideEntity guide = guideRepository.findByPublicId(ownerId)
                    .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
            if (guide.getAuthor() != null
                    && currentUser.getPublicId().equals(guide.getAuthor().getPublicId())) {
                return;
            }
        }

        throw new ApiException(ExceptionResponse.Forbidden);
    }
}
