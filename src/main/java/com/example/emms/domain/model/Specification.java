package com.example.emms.domain.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Specification {
    private String parameters; //组件的详细规格（电压、容量）

    // 默认构造函数（JPA要求）
    public Specification() {}

    // 带参构造函数
    public Specification(String parameters) {
        this.parameters = parameters;
    }
}