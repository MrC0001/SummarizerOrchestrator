package com.local.SummarizerOrchestrator.models;

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
@Table(name = "transcripts")
public class Transcript extends BaseEntity {

    private String scenario;

    @Lob
    @Column(name = "transcript", length = 10_000) // example length, adjust as needed
    private String transcript;
}
