package com.local.SummarizerOrchestrator.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "metrics")
public class Metrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transcript_id", nullable = false)
    private Transcript transcript;

    @ManyToOne
    @JoinColumn(name = "summary_id", nullable = false)
    private Summary summary;

    @Column(name = "rouge1")
    private Float rouge1;

    @Column(name = "rouge2")
    private Float rouge2;

    @Column(name = "rougeL")
    private Float rougeL;

    @Column(name = "bert_precision")
    private Float bertPrecision;

    @Column(name = "bert_recall")
    private Float bertRecall;

    @Column(name = "bert_f1")
    private Float bertF1;

    @Column(name = "bleu")
    private Float bleu;

    @Column(name = "meteor")
    private Float meteor;

    @Column(name = "length_ratio")
    private Float lengthRatio;

    @Column(name = "redundancy")
    private Float redundancy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
