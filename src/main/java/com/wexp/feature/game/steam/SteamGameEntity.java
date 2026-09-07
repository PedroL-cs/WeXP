package com.wexp.feature.game.steam;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "steam_games")
public class SteamGameEntity {

    @Id
    private Long appid;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 100)
    private String acronym;

    @Transient
    private String capsuleUrl;

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
