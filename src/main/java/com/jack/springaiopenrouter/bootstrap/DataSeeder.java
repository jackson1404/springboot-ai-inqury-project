package com.jack.springaiopenrouter.bootstrap;

import com.jack.springaiopenrouter.entity.CustomerEntity;
import com.jack.springaiopenrouter.entity.OrderEntity;
import com.jack.springaiopenrouter.entity.ProductEntity;
import com.jack.springaiopenrouter.entity.UserEntity;
import com.jack.springaiopenrouter.entity.UserRole;
import com.jack.springaiopenrouter.repository.CustomerRepository;
import com.jack.springaiopenrouter.repository.OrderRepository;
import com.jack.springaiopenrouter.repository.ProductRepository;
import com.jack.springaiopenrouter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;

    public DataSeeder(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:true}") boolean seedEnabled
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        seedUsers();
        seedCustomers();
        seedProducts();
        seedOrders();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.saveAll(List.of(
                new UserEntity("jack@example.com", "Jack Son", passwordEncoder.encode("Password123"), UserRole.ADMIN),
                new UserEntity("demo@example.com", "Demo User", passwordEncoder.encode("Password123"), UserRole.USER)
        ));
    }

    private void seedCustomers() {
        if (customerRepository.count() > 0) {
            return;
        }

        customerRepository.saveAll(List.of(
                new CustomerEntity("CUST-1001", "Jack Son", "jack@example.com", "Gold", "Myanmar"),
                new CustomerEntity("CUST-1002", "Maya Chen", "maya@example.com", "Silver", "Singapore"),
                new CustomerEntity("CUST-1003", "Alex Kim", "alex@example.com", "Platinum", "Thailand")
        ));
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.saveAll(List.of(
                new ProductEntity("AI-STARTER", "AI Starter Plan", "Subscription", new BigDecimal("19.00"), 999),
                new ProductEntity("RAG-PRO", "RAG Pro Knowledge Base", "AI Platform", new BigDecimal("99.00"), 120),
                new ProductEntity("AGENT-OPS", "Agent Operations Toolkit", "AI Platform", new BigDecimal("149.00"), 64),
                new ProductEntity("DEV-SUPPORT", "Developer Support Package", "Service", new BigDecimal("49.00"), 30)
        ));
    }

    private void seedOrders() {
        if (orderRepository.count() > 0) {
            return;
        }

        orderRepository.saveAll(List.of(
                new OrderEntity("ORD-9001", "CUST-1001", "AI-STARTER", 1, new BigDecimal("19.00"), "PAID", LocalDate.of(2026, 5, 10)),
                new OrderEntity("ORD-9002", "CUST-1001", "RAG-PRO", 1, new BigDecimal("99.00"), "PROCESSING", LocalDate.of(2026, 5, 12)),
                new OrderEntity("ORD-9003", "CUST-1002", "DEV-SUPPORT", 2, new BigDecimal("98.00"), "SHIPPED", LocalDate.of(2026, 5, 15)),
                new OrderEntity("ORD-9004", "CUST-1003", "AGENT-OPS", 1, new BigDecimal("149.00"), "PAID", LocalDate.of(2026, 5, 18))
        ));
    }
}
