package com.jack.springaiopenrouter.model;

import java.math.BigDecimal;

public record ProductRecord(
        String code,
        String name,
        String category,
        BigDecimal price,
        int stock
) {}
