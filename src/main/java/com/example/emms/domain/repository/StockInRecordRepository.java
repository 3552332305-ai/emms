package com.example.emms.domain.repository;

import com.example.emms.domain.model.StockInRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockInRecordRepository extends JpaRepository<StockInRecord, Long> {
    List<StockInRecord> findByComponentTypeId(Long componentTypeId);

    @Query("SELECT SUM(s.quantity) FROM StockInRecord s WHERE s.componentType.id = :componentTypeId")
    Integer sumQuantityByComponentTypeId(@Param("componentTypeId") Long componentTypeId);
}
