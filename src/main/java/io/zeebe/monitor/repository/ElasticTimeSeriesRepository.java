package io.zeebe.monitor.repository;

import io.zeebe.monitor.entity.ElasticTimeSeries;
import io.zeebe.monitor.entity.StatusWork;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public interface ElasticTimeSeriesRepository extends CrudRepository<ElasticTimeSeries, Long> {
    @Query("SELECT MAX(e.endTime) FROM ElasticTimeSeries e")
    Optional<Long> findMaxEndTime();

    @Transactional
    default Optional<ElasticTimeSeries> createNextTimeSeriesIfPossible(long delta, long firstStartTime) {
        var maxEndTime = findMaxEndTime();
        long startTime = maxEndTime.map(aLong -> aLong + 1).orElse(firstStartTime);
        long endTime = startTime + delta;

        var isGreaterThenNow = endTime > System.currentTimeMillis();
        if(isGreaterThenNow) {
            return Optional.empty();
        }

        ElasticTimeSeries timeSeries = new ElasticTimeSeries();
        timeSeries.setStartTime(startTime);
        timeSeries.setEndTime(endTime);
        timeSeries.setStatus(StatusWork.IN_WORK);
        timeSeries.setStartWorkerTime(new Timestamp(System.currentTimeMillis()).getTime());

        return Optional.of(save(timeSeries));
    }
}
