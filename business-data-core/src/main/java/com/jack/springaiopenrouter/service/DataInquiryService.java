package com.jack.springaiopenrouter.service;

import com.jack.springaiopenrouter.entity.CustomerEntity;
import com.jack.springaiopenrouter.entity.OrderEntity;
import com.jack.springaiopenrouter.entity.ProductEntity;
import com.jack.springaiopenrouter.model.CustomerRecord;
import com.jack.springaiopenrouter.model.OrderRecord;
import com.jack.springaiopenrouter.model.ProductRecord;
import com.jack.springaiopenrouter.repository.CustomerRepository;
import com.jack.springaiopenrouter.repository.OrderRepository;
import com.jack.springaiopenrouter.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DataInquiryService {

    private static final Logger log = LoggerFactory.getLogger(DataInquiryService.class);

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public DataInquiryService(
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository
    ) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<CustomerRecord> searchCustomers(String query) {
        String normalizedQuery = safeQuery(query);
        log.info("Business data query started: operation=searchCustomers, query={}", safeForLog(normalizedQuery));

        List<CustomerRecord> result = customerRepository.search(normalizedQuery).stream()
                .map(this::toCustomerRecord)
                .toList();

        log.info("Business data query completed: operation=searchCustomers, resultCount={}", result.size());
        return result;
    }

    public List<OrderRecord> searchOrders(String query) {
        String normalizedQuery = safeQuery(query);
        log.info("Business data query started: operation=searchOrders, query={}", safeForLog(normalizedQuery));

        List<OrderRecord> result = orderRepository.search(normalizedQuery).stream()
                .map(this::toOrderRecord)
                .toList();

        log.info("Business data query completed: operation=searchOrders, resultCount={}", result.size());
        return result;
    }

    public List<ProductRecord> searchProducts(String query) {
        String normalizedQuery = safeQuery(query);
        log.info("Business data query started: operation=searchProducts, query={}", safeForLog(normalizedQuery));

        List<ProductRecord> result = productRepository.search(normalizedQuery).stream()
                .map(this::toProductRecord)
                .toList();

        log.info("Business data query completed: operation=searchProducts, resultCount={}", result.size());
        return result;
    }

    public List<CustomerRecord> allCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toCustomerRecord)
                .toList();
    }

    public List<OrderRecord> allOrders() {
        return orderRepository.findAll().stream()
                .map(this::toOrderRecord)
                .toList();
    }

    public List<ProductRecord> allProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductRecord)
                .toList();
    }

    public List<OrderRecord> ordersByCustomerId(String customerId) {
        String normalizedCustomerId = safeQuery(customerId);
        log.info("Business data query started: operation=ordersByCustomerId, customerId={}", safeForLog(normalizedCustomerId));

        List<OrderRecord> result = orderRepository.findByCustomerIdIgnoreCaseOrderByOrderDateDesc(normalizedCustomerId).stream()
                .map(this::toOrderRecord)
                .toList();

        log.info("Business data query completed: operation=ordersByCustomerId, resultCount={}", result.size());
        return result;
    }

    public BigDecimal totalSpendByCustomerId(String customerId) {
        String normalizedCustomerId = safeQuery(customerId);
        log.info("Business data query started: operation=totalSpendByCustomerId, customerId={}", safeForLog(normalizedCustomerId));

        BigDecimal totalSpend = orderRepository.totalSpendByCustomerId(normalizedCustomerId);

        log.info("Business data query completed: operation=totalSpendByCustomerId, totalSpend={}", totalSpend);
        return totalSpend;
    }

    public long orderCountByCustomerId(String customerId) {
        return orderRepository.findByCustomerIdIgnoreCaseOrderByOrderDateDesc(safeQuery(customerId)).size();
    }

    private String safeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private String safeForLog(String value) {
        if (value == null) {
            return "<null>";
        }

        String normalized = value.replaceAll("[\r\n\t]", " ").trim();
        if (normalized.length() <= 120) {
            return normalized;
        }

        return normalized.substring(0, 120) + "...";
    }

    private CustomerRecord toCustomerRecord(CustomerEntity entity) {
        return new CustomerRecord(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getTier(),
                entity.getRegion()
        );
    }

    private ProductRecord toProductRecord(ProductEntity entity) {
        return new ProductRecord(
                entity.getCode(),
                entity.getName(),
                entity.getCategory(),
                entity.getPrice(),
                entity.getStock()
        );
    }

    private OrderRecord toOrderRecord(OrderEntity entity) {
        return new OrderRecord(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductCode(),
                entity.getQuantity(),
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getOrderDate()
        );
    }
}
