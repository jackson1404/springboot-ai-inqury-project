package com.jack.springaiopenrouter.service;

import com.jack.springaiopenrouter.entity.CustomerEntity;
import com.jack.springaiopenrouter.entity.OrderEntity;
import com.jack.springaiopenrouter.entity.ProductEntity;
import com.jack.springaiopenrouter.repository.CustomerRepository;
import com.jack.springaiopenrouter.repository.OrderRepository;
import com.jack.springaiopenrouter.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:data-service-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(DataInquiryService.class)
class DataInquiryServiceTests {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DataInquiryService dataInquiryService;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        customerRepository.saveAll(List.of(
                new CustomerEntity("CUST-1001", "Jack Son", "jack@example.com", "Gold", "Myanmar"),
                new CustomerEntity("CUST-1002", "Maya Chen", "maya@example.com", "Silver", "Singapore")
        ));
        productRepository.saveAll(List.of(
                new ProductEntity("AI-STARTER", "AI Starter Plan", "Subscription", new BigDecimal("19.00"), 999),
                new ProductEntity("RAG-PRO", "RAG Pro Knowledge Base", "AI Platform", new BigDecimal("99.00"), 120)
        ));
        orderRepository.saveAll(List.of(
                new OrderEntity("ORD-9001", "CUST-1001", "AI-STARTER", 1, new BigDecimal("19.00"), "PAID", LocalDate.of(2026, 5, 10)),
                new OrderEntity("ORD-9002", "CUST-1001", "RAG-PRO", 1, new BigDecimal("99.00"), "PROCESSING", LocalDate.of(2026, 5, 12))
        ));
    }

    @Test
    void searchesCustomersOrdersAndProducts() {
        assertThat(dataInquiryService.searchCustomers("gold"))
                .extracting("id")
                .containsExactly("CUST-1001");

        assertThat(dataInquiryService.searchOrders("CUST-1001"))
                .extracting("id")
                .containsExactly("ORD-9002", "ORD-9001");

        assertThat(dataInquiryService.searchProducts("rag"))
                .extracting("code")
                .containsExactly("RAG-PRO");
    }

    @Test
    void calculatesTotalSpendByCustomerId() {
        assertThat(dataInquiryService.totalSpendByCustomerId("cust-1001"))
                .isEqualByComparingTo("118.00");

        assertThat(dataInquiryService.orderCountByCustomerId("cust-1001"))
                .isEqualTo(2);
    }
}
