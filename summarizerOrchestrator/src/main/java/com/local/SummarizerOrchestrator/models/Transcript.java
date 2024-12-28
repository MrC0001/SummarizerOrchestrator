package com.local.SummarizerOrchestrator.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transcripts")
public class Transcript extends BaseEntity {

    private String scenario;

    @Lob
    @Column(name = "transcript", length = 10_000)
    private String transcript;

    @OneToMany(mappedBy = "transcript", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Marks the parent side of the relationship
    private List<Summary> summaries;
}
