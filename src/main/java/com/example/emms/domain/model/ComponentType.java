package com.example.emms.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "component_type")
@Getter
@Setter
public class ComponentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String model;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // 直接存储供应商ID，便于查询
    @Column(name = "supplier_id", insertable = false, updatable = false)
    private Long supplierId;

    private Double unitPrice;
    private String image;

    @Embedded
    private Specification specification;

    // 审核状态: PENDING, APPROVED, REJECTED
    private String status = "PENDING";

    // 添加库存数量字段
    private Integer stockQuantity = 0;

    // 默认构造函数
    public ComponentType() {}

    // 检查库存是否充足
    public boolean isStockSufficient(int requiredQuantity) {
        return this.stockQuantity != null && this.stockQuantity >= requiredQuantity;
    }

    // 减少库存
    public void decreaseStock(int quantity) {
        if (this.stockQuantity != null && this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
        }
    }

    // 增加库存
    public void increaseStock(int quantity) {
        if (this.stockQuantity == null) {
            this.stockQuantity = 0;
        }
        this.stockQuantity += quantity;
    }

    // 添加获取供应商ID的方法
    public Long getSupplierId() {
        return this.supplierId;
    }

    // 添加设置供应商ID的方法
    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }
}
