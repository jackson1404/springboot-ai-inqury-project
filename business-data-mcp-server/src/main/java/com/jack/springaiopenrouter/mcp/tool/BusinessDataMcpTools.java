package com.jack.springaiopenrouter.mcp.tool;

import com.jack.springaiopenrouter.model.CustomerRecord;
import com.jack.springaiopenrouter.model.OrderRecord;
import com.jack.springaiopenrouter.model.ProductRecord;
import com.jack.springaiopenrouter.service.DataInquiryService;
import org.springframework.stereotype.Component;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import java.util.List;

@Component
public class BusinessDataMcpTools {

    private final DataInquiryService dataInquiryService;

    public BusinessDataMcpTools(DataInquiryService dataInquiryService) {
        this.dataInquiryService = dataInquiryService;
    }

    @McpTool(
            name = "search_customers",
            description = """
                Search customer records from PostgreSQL by customer id, name, email, tier, or region.
                Use this tool when the user asks to find, list, or look up customers.
                Returns matching customer records only; does not create, update, or delete customers.
                """
    )
    public List<CustomerRecord> searchCustomers(
            @McpToolParam(description = "Customer search text. Can be a customer id, name, email, tier, or region.", required = true)
            String query
    ) {
        return dataInquiryService.searchCustomers(query);
    }

    @McpTool(
            name = "search_orders",
            description = "Search orders from the PostgreSQL database by order id, customer id, product code, or order status."
    )
    public List<OrderRecord> searchOrders(
            @McpToolParam(description = "Order search text. Can be an order id, customer id, product code, or order status.", required = true)
            String query
    ) {
        return dataInquiryService.searchOrders(query);
    }

    @McpTool(
            name = "search_products",
            description = "Search products from the PostgreSQL database by product code, name, or category."
    )
    public List<ProductRecord> searchProducts(
            @McpToolParam(description = "Product search text. Can be a product code, product name, or category.", required = true)
            String query
    ) {
        return dataInquiryService.searchProducts(query);
    }

    @McpTool(
            name = "calculate_customer_total_spend",
            description = "Calculate total amount spent by a customer using PostgreSQL order data. Input must be a customer id like CUST-1001."
    )
    public String calculateCustomerTotalSpend(
            @McpToolParam(description = "Customer id, for example CUST-1001.", required = true)
            String customerId
    ) {
        var orders = dataInquiryService.ordersByCustomerId(customerId);
        var total = dataInquiryService.totalSpendByCustomerId(customerId);

        return "Customer " + customerId + " has " + orders.size() + " orders and total spend is USD " + total + ".";
    }
}
