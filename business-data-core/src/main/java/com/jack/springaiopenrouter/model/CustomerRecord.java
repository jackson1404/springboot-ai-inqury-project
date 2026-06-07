package com.jack.springaiopenrouter.model;

public record CustomerRecord(
        String id,
        String name,
        String email,
        String tier,
        String region
) {}
