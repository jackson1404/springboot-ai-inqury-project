package com.jack.springaiopenrouter.tool;

import com.jack.springaiopenrouter.model.CustomerRecord;
import com.jack.springaiopenrouter.model.OrderRecord;
import com.jack.springaiopenrouter.model.ProductRecord;
import com.jack.springaiopenrouter.service.DataInquiryService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseBusinessTools {

    private final DataInquiryService dataInquiryService;

    public DatabaseBusinessTools(DataInquiryService dataInquiryService) {
        this.dataInquiryService = dataInquiryService;
    }

    @Tool(description = "Search customers from the PostgreSQL database by customer id, name, email, tier, or region. Use when the user asks about customers.")
    public List<CustomerRecord> searchCustomers(String query) {
        return dataInquiryService.searchCustomers(query);
    }

    @Tool(description = "Search orders from the PostgreSQL database by order id, customer id, product code, or order status. Use when the user asks about orders, purchases, sales, or order status.")
    public List<OrderRecord> searchOrders(String query) {
        return dataInquiryService.searchOrders(query);
    }

    @Tool(description = "Search products from the PostgreSQL database by product code, name, or category. Use when the user asks about products, prices, categories, or stock.")
    public List<ProductRecord> searchProducts(String query) {
        return dataInquiryService.searchProducts(query);
    }

    @Tool(description = "Calculate total amount spent by a customer using PostgreSQL order data. Input must be a customer id like CUST-1001.")
    public String calculateCustomerTotalSpend(String customerId) {
        var orders = dataInquiryService.ordersByCustomerId(customerId);
        var total = dataInquiryService.totalSpendByCustomerId(customerId);

        return "Customer " + customerId + " has " + orders.size() + " orders and total spend is USD " + total + ".";
    }
}
