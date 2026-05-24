package com.jack.springaiopenrouter.repository;

import com.jack.springaiopenrouter.model.CustomerRecord;
import com.jack.springaiopenrouter.model.OrderRecord;
import com.jack.springaiopenrouter.model.ProductRecord;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class DemoDataRepository {

    private final List<CustomerRecord> customers = List.of(
            new CustomerRecord("CUST-1001", "Jack Son", "jack@example.com", "Gold", "Myanmar"),
            new CustomerRecord("CUST-1002", "Maya Chen", "maya@example.com", "Silver", "Singapore"),
            new CustomerRecord("CUST-1003", "Alex Kim", "alex@example.com", "Platinum", "Thailand")
    );

    private final List<ProductRecord> products = List.of(
            new ProductRecord("AI-STARTER", "AI Starter Plan", "Subscription", new BigDecimal("19.00"), 999),
            new ProductRecord("RAG-PRO", "RAG Pro Knowledge Base", "AI Platform", new BigDecimal("99.00"), 120),
            new ProductRecord("AGENT-OPS", "Agent Operations Toolkit", "AI Platform", new BigDecimal("149.00"), 64),
            new ProductRecord("DEV-SUPPORT", "Developer Support Package", "Service", new BigDecimal("49.00"), 30)
    );

    private final List<OrderRecord> orders = List.of(
            new OrderRecord("ORD-9001", "CUST-1001", "AI-STARTER", 1, new BigDecimal("19.00"), "PAID", LocalDate.of(2026, 5, 10)),
            new OrderRecord("ORD-9002", "CUST-1001", "RAG-PRO", 1, new BigDecimal("99.00"), "PROCESSING", LocalDate.of(2026, 5, 12)),
            new OrderRecord("ORD-9003", "CUST-1002", "DEV-SUPPORT", 2, new BigDecimal("98.00"), "SHIPPED", LocalDate.of(2026, 5, 15)),
            new OrderRecord("ORD-9004", "CUST-1003", "AGENT-OPS", 1, new BigDecimal("149.00"), "PAID", LocalDate.of(2026, 5, 18))
    );

    public List<CustomerRecord> findCustomers(String query) {
        String q = normalize(query);
        return customers.stream()
                .filter(c -> normalize(c.id()).contains(q)
                        || normalize(c.name()).contains(q)
                        || normalize(c.email()).contains(q)
                        || normalize(c.tier()).contains(q)
                        || normalize(c.region()).contains(q))
                .toList();
    }

    public List<ProductRecord> findProducts(String query) {
        String q = normalize(query);
        return products.stream()
                .filter(p -> normalize(p.code()).contains(q)
                        || normalize(p.name()).contains(q)
                        || normalize(p.category()).contains(q))
                .sorted(Comparator.comparing(ProductRecord::code))
                .toList();
    }

    public List<OrderRecord> findOrders(String query) {
        String q = normalize(query);
        return orders.stream()
                .filter(o -> normalize(o.id()).contains(q)
                        || normalize(o.customerId()).contains(q)
                        || normalize(o.productCode()).contains(q)
                        || normalize(o.status()).contains(q))
                .sorted(Comparator.comparing(OrderRecord::orderDate).reversed())
                .toList();
    }

    public Optional<ProductRecord> findProductByCode(String code) {
        String q = normalize(code);
        return products.stream().filter(p -> normalize(p.code()).equals(q)).findFirst();
    }

    public List<CustomerRecord> allCustomers() {
        return customers;
    }

    public List<ProductRecord> allProducts() {
        return products;
    }

    public List<OrderRecord> allOrders() {
        return orders;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
