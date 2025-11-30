package com.example.emms.domain.repository;

import com.example.emms.domain.model.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentTypeRepository extends JpaRepository<ComponentType, Long> {
    Optional<ComponentType> findByModel(String model);
    
    // 查找第一个匹配的型号（优先返回已审核通过的）
    @Query("SELECT ct FROM ComponentType ct WHERE ct.model = :model ORDER BY CASE WHEN ct.status = 'APPROVED' THEN 0 ELSE 1 END, ct.id ASC")
    List<ComponentType> findAllByModel(@Param("model") String model);
    
    boolean existsByModel(String model);
    List<ComponentType> findByNameContaining(String name);

    // 根据供应商ID查询
    @Query("SELECT ct FROM ComponentType ct WHERE ct.supplier.id = :supplierId")
    List<ComponentType> findBySupplierId(@Param("supplierId") Long supplierId);

    // 根据状态查询
    List<ComponentType> findByStatus(String status);

    // 查询库存充足的元器件
    List<ComponentType> findByStockQuantityGreaterThan(Integer quantity);
}
