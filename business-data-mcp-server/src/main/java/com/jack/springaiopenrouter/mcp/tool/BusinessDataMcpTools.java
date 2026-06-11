package com.jack.springaiopenrouter.mcp.tool;

import com.jack.springaiopenrouter.model.CustomerRecord;
import com.jack.springaiopenrouter.model.OrderRecord;
import com.jack.springaiopenrouter.model.ProductRecord;
import com.jack.springaiopenrouter.service.DataInquiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import java.util.List;

@Component
public class BusinessDataMcpTools {

    private static final Logger log = LoggerFactory.getLogger(BusinessDataMcpTools.class);

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
        long startNanos = System.nanoTime();
        log.info("MCP tool request received: tool=search_customers, query={}", safeForLog(query));

        try {
            List<CustomerRecord> result = dataInquiryService.searchCustomers(query);
            log.info("MCP tool request completed: tool=search_customers, resultCount={}, durationMs={}",
                    result.size(), elapsedMillis(startNanos));
            return result;
        } catch (RuntimeException error) {
            log.error("MCP tool request failed: tool=search_customers, durationMs={}, error={}",
                    elapsedMillis(startNanos), error.getMessage(), error);
            throw error;
        }
    }

    @McpTool(
            name = "search_orders",
            description = "Search orders from the PostgreSQL database by order id, customer id, product code, or order status."
    )
    public List<OrderRecord> searchOrders(
            @McpToolParam(description = "Order search text. Can be an order id, customer id, product code, or order status.", required = true)
            String query
    ) {
        long startNanos = System.nanoTime();
        log.info("MCP tool request received: tool=search_orders, query={}", safeForLog(query));

        try {
            List<OrderRecord> result = dataInquiryService.searchOrders(query);
            log.info("MCP tool request completed: tool=search_orders, resultCount={}, durationMs={}",
                    result.size(), elapsedMillis(startNanos));
            return result;
        } catch (RuntimeException error) {
            log.error("MCP tool request failed: tool=search_orders, durationMs={}, error={}",
                    elapsedMillis(startNanos), error.getMessage(), error);
            throw error;
        }
    }

    @McpTool(
            name = "search_products",
            description = "Search products from the PostgreSQL database by product code, name, or category."
    )
    public List<ProductRecord> searchProducts(
            @McpToolParam(description = "Product search text. Can be a product code, product name, or category.", required = true)
            String query
    ) {
        long startNanos = System.nanoTime();
        log.info("MCP tool request received: tool=search_products, query={}", safeForLog(query));

        try {
            List<ProductRecord> result = dataInquiryService.searchProducts(query);
            log.info("MCP tool request completed: tool=search_products, resultCount={}, durationMs={}",
                    result.size(), elapsedMillis(startNanos));
            return result;
        } catch (RuntimeException error) {
            log.error("MCP tool request failed: tool=search_products, durationMs={}, error={}",
                    elapsedMillis(startNanos), error.getMessage(), error);
            throw error;
        }
    }

    @McpTool(
            name = "calculate_customer_total_spend",
            description = "Calculate total amount spent by a customer using PostgreSQL order data. Input must be a customer id like CUST-1001."
    )
    public String calculateCustomerTotalSpend(
            @McpToolParam(description = "Customer id, for example CUST-1001.", required = true)
            String customerId
    ) {
        long startNanos = System.nanoTime();
        log.info("MCP tool request received: tool=calculate_customer_total_spend, customerId={}", safeForLog(customerId));

        try {
            var orders = dataInquiryService.ordersByCustomerId(customerId);
            var total = dataInquiryService.totalSpendByCustomerId(customerId);
            String response = "Customer " + customerId + " has " + orders.size() + " orders and total spend is USD " + total + ".";

            log.info("MCP tool request completed: tool=calculate_customer_total_spend, orderCount={}, totalSpend={}, durationMs={}",
                    orders.size(), total, elapsedMillis(startNanos));
            return response;
        } catch (RuntimeException error) {
            log.error("MCP tool request failed: tool=calculate_customer_total_spend, durationMs={}, error={}",
                    elapsedMillis(startNanos), error.getMessage(), error);
            throw error;
        }
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

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
