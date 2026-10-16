package com.wexp.feature.game.steam;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "steam_games")
public class SteamGameEntity implements Persistable<Long> {

    @Id
    private Long appid;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 100)
    private String acronym;

    @Transient
    private String capsuleUrl;

    @Transient
    @Builder.Default
    private boolean newEntity = true;

    @Override
    public Long getId() {
        return appid;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PostLoad
    public void markNotNew() {
        this.newEntity = false;
    }

    @PrePersist
    @PreUpdate
    public void generateAcronym() {
        if (this.name == null || this.name.isBlank()) {
            this.acronym = "";
            return;
        }

        String[] words = this.name.split("[\\s\\-_:]+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;

            if (word.matches("^[A-Z0-9]+$")) {
                sb.append(word);
            } else {
                StringBuilder wordChars = new StringBuilder();
                for (char c : word.toCharArray()) {
                    if (Character.isUpperCase(c) || Character.isDigit(c)) {
                        wordChars.append(c);
                    }
                }

                if (!wordChars.isEmpty()) {
                    sb.append(wordChars);
                } else {
                    sb.append(Character.toUpperCase(word.charAt(0)));
                }
            }
        }

        String generatedAcronym = sb.toString();

        this.acronym = generatedAcronym.length() > 100
                ? generatedAcronym.substring(0, 100)
                : generatedAcronym;
    }
}
