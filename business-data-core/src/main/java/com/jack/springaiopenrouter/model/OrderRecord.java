package com.jack.springaiopenrouter.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderRecord(
        String id,
        String customerId,
        String productCode,
        int quantity,
        BigDecimal totalAmount,
        String status,
        LocalDate orderDate
) {}
