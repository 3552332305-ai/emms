package com.example.emms.domain.repository;

import com.example.emms.domain.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // 根据名称查找供应商
    List<Supplier> findByNameContaining(String name);

    // 检查供应商名称是否存在
    boolean existsByName(String name);

    // 统计供应商的元器件类型数量
    @Query("SELECT s.name, COUNT(ct.id) FROM Supplier s LEFT JOIN ComponentType ct ON s.id = ct.supplier.id GROUP BY s.id, s.name")
    List<Object[]> countComponentTypesBySupplier();
}
