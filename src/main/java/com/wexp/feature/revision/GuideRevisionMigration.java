package com.wexp.feature.revision;

import com.wexp.feature.guide.IGuideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GuideRevisionMigration implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(GuideRevisionMigration.class);
    private final IGuideRepository guides;
    private final GuideRevisionLifecycleService lifecycle;

    @Override
    public void run(ApplicationArguments args) {
        List<String> pending;
        int migrated = 0;
        do {
            pending = guides.findGuidesNeedingRevisionMigration(GuideRevisionLifecycleService.LEGACY_STATUSES, PageRequest.of(0, 100));
            pending.forEach(lifecycle::migrateGuide);
            migrated += pending.size();
        } while (!pending.isEmpty());
        if (migrated > 0) logger.info("Versões originais e revisões legadas inicializadas para {} guias", migrated);
    }
}
