package com.wexp.feature.game.steam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ISteamGameRepository extends JpaRepository<SteamGameEntity, Long> {
    Optional<SteamGameEntity> findFirstBy();

    @Query("""
            SELECT s FROM SteamGameEntity s
            WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR (:cleanQuery <> '' AND LOWER(s.acronym) LIKE LOWER(CONCAT('%', :cleanQuery, '%')))
            """)
    Page<SteamGameEntity> searchByNameOrAcronym(
            @Param("query") String query,
            @Param("cleanQuery") String cleanQuery,
            Pageable pageable);
}
