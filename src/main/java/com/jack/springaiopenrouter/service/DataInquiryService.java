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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DataInquiryService {

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
        return customerRepository.search(safeQuery(query)).stream()
                .map(this::toCustomerRecord)
                .toList();
    }

    public List<OrderRecord> searchOrders(String query) {
        return orderRepository.search(safeQuery(query)).stream()
                .map(this::toOrderRecord)
                .toList();
    }

    public List<ProductRecord> searchProducts(String query) {
        return productRepository.search(safeQuery(query)).stream()
                .map(this::toProductRecord)
                .toList();
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
        return orderRepository.findByCustomerIdIgnoreCaseOrderByOrderDateDesc(safeQuery(customerId)).stream()
                .map(this::toOrderRecord)
                .toList();
    }

    public BigDecimal totalSpendByCustomerId(String customerId) {
        return orderRepository.totalSpendByCustomerId(safeQuery(customerId));
    }

    public long orderCountByCustomerId(String customerId) {
        return orderRepository.findByCustomerIdIgnoreCaseOrderByOrderDateDesc(safeQuery(customerId)).size();
    }

    private String safeQuery(String query) {
        return query == null ? "" : query.trim();
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
