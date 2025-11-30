package com.example.emms.domain.repository;

import com.example.emms.domain.model.StockOutRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockOutRecordRepository extends JpaRepository<StockOutRecord, Long> {
    List<StockOutRecord> findByComponentTypeId(Long componentTypeId);
    List<StockOutRecord> findByTakenBy(String takenBy);
    List<StockOutRecord> findByStatus(String status);

    // 分页查询
    Page<StockOutRecord> findAll(Pageable pageable);

    @Query("SELECT SUM(s.quantity) FROM StockOutRecord s WHERE s.componentType.id = :componentTypeId")
    Integer sumQuantityByComponentTypeId(@Param("componentTypeId") Long componentTypeId);


}