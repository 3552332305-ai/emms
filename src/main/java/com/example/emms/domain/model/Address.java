package com.example.emms.domain.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Address {
    private String province;
    private String city;
    private String detail;

    // 默认构造函数
    public Address() {}

    // 带参构造函数
    public Address(String province, String city, String detail) {
        this.province = province;
        this.city = city;
        this.detail = detail;
    }
}