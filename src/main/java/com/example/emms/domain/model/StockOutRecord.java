package com.example.emms.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_out_record")
@Getter
@Setter
public class StockOutRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "component_type_id", nullable = false)
    private ComponentType componentType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String takenBy;

    @Column(nullable = false)
    private LocalDateTime takeDate = LocalDateTime.now();

    // 添加审核状态: PENDING, APPROVED, REJECTED
    private String status = "PENDING";

    // 审核人
    private String approvedBy;

    // 审核时间
    private LocalDateTime approvedAt;

    // 拒绝原因
    private String rejectionReason;
}