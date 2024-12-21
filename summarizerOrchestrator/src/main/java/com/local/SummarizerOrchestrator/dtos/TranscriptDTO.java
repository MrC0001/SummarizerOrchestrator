package com.local.SummarizerOrchestrator.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptDTO {
    private Long id;
    private String scenario;
    private String transcript;
}
