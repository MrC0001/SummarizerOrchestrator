package com.local.SummarizerOrchestrator.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "summaries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"transcript_id", "provider_name"})
})
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", nullable = false)
    @JsonBackReference // Marks the child side of the relationship
    private Transcript transcript;

    private String providerName;

    @Lob
    @Column(name = "summary_text", length = 10_000) // Match Transcript style
    private String summaryText;
}
