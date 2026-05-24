package com.jack.springaiopenrouter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "customer_orders")
public class OrderEntity {

    @Id
    @Column(length = 50, nullable = false)
    private String id;

    @Column(nullable = false, length = 40)
    private String customerId;

    @Column(nullable = false, length = 60)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    private LocalDate orderDate;

    protected OrderEntity() {
        // Required by JPA
    }

    public OrderEntity(String id, String customerId, String productCode, int quantity, BigDecimal totalAmount, String status, LocalDate orderDate) {
        this.id = id;
        this.customerId = customerId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }
}
