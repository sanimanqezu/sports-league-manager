package com.sanele.sportsleague.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false, unique = true)
    private Fixture fixture;

    @Min(0)
    @Column(name = "home_goals", nullable = false)
    private int homeGoals;

    @Min(0)
    @Column(name = "away_goals", nullable = false)
    private int awayGoals;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onRecord() {
        this.recordedAt = LocalDateTime.now();
    }
}
