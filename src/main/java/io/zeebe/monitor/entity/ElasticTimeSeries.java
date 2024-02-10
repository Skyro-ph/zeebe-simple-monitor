package io.zeebe.monitor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "elastic_time_series")
public class ElasticTimeSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long startTime;

    @Column(nullable = false)
    private long endTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusWork status;

    @Column(nullable = false)
    private long startWorkerTime;

    //Optional field if raise exception while worker run, -1 if was completely unprocessed
    private long lastProcessedEvent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public StatusWork getStatus() {
        return status;
    }

    public void setStatus(StatusWork status) {
        this.status = status;
    }

    public long getStartWorkerTime() {
        return startWorkerTime;
    }

    public void setStartWorkerTime(long startWorkerTime) {
        this.startWorkerTime = startWorkerTime;
    }

    public long getLastProcessedEvent() {
        return lastProcessedEvent;
    }

    public void setLastProcessedEvent(long lastProcessedEvent) {
        this.lastProcessedEvent = lastProcessedEvent;
    }
}
