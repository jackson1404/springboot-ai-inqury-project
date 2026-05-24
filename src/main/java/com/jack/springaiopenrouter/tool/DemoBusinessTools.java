package com.jack.springaiopenrouter.tool;

import com.jack.springaiopenrouter.model.CustomerRecord;
import com.jack.springaiopenrouter.model.OrderRecord;
import com.jack.springaiopenrouter.model.ProductRecord;
import com.jack.springaiopenrouter.repository.DemoDataRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoBusinessTools {

    private final DemoDataRepository repository;

    public DemoBusinessTools(DemoDataRepository repository) {
        this.repository = repository;
    }

    @Tool(description = "Search demo customers by customer id, name, email, tier, or region. Use when the user asks about customers.")
    public List<CustomerRecord> searchCustomers(String query) {
        return repository.findCustomers(query);
    }

    @Tool(description = "Search demo orders by order id, customer id, product code, or order status. Use when the user asks about orders, purchases, sales, or order status.")
    public List<OrderRecord> searchOrders(String query) {
        return repository.findOrders(query);
    }

    @Tool(description = "Search demo products by product code, name, or category. Use when the user asks about products, prices, categories, or stock.")
    public List<ProductRecord> searchProducts(String query) {
        return repository.findProducts(query);
    }

    @Tool(description = "Calculate the total amount spent by a demo customer. Input must be a customer id like CUST-1001.")
    public String calculateCustomerTotalSpend(String customerId) {
        var orders = repository.findOrders(customerId);
        var total = orders.stream()
                .filter(order -> order.customerId().equalsIgnoreCase(customerId))
                .map(OrderRecord::totalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return "Customer " + customerId + " has " + orders.size() + " matching orders and total spend is USD " + total + ".";
    }
}
