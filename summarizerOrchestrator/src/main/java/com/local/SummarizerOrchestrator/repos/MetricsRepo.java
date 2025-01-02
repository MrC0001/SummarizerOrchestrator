package com.local.SummarizerOrchestrator.repos;

import com.local.SummarizerOrchestrator.models.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricsRepo extends JpaRepository<Metrics, Long> {
}
