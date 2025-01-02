package com.local.SummarizerOrchestrator.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a summary entity.
 *
 * Validation Rules:
 * - `@NotNull`: Ensures fields are not null.
 * - `@NotBlank`: Ensures string fields are not blank.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "summaries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"transcript_id", "provider_name"})
})
public class Summary extends BaseEntity{


    /**
     * The transcript associated with this summary.
     * Must not be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", nullable = false)
    @NotNull(message = "Transcript must not be null.")
    @JsonBackReference // Prevents circular references in bidirectional relationships
    private Transcript transcript;

    /**
     * The name of the provider that generated this summary.
     * Must not be blank.
     */
    @NotBlank(message = "Provider name must not be blank.")
    @Column(name = "provider_name", nullable = false)
    private String providerName;

    /**
     * The text content of the summary.
     * Must not be blank.
     */
    @Lob
    @Column(name = "summary_text", length = 10_000) // Matches Transcript text style
    @NotBlank(message = "Summary text must not be blank.")
    private String summaryText;

    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Metrics> metrics;
}
