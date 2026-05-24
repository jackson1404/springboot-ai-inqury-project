package com.jack.springaiopenrouter.controller;

import com.jack.springaiopenrouter.dto.DataInquiryRequest;
import com.jack.springaiopenrouter.model.CustomerRecord;
import com.jack.springaiopenrouter.model.OrderRecord;
import com.jack.springaiopenrouter.model.ProductRecord;
import com.jack.springaiopenrouter.service.DataInquiryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataInquiryController {

    private final DataInquiryService dataInquiryService;

    public DataInquiryController(DataInquiryService dataInquiryService) {
        this.dataInquiryService = dataInquiryService;
    }

    @GetMapping("/customers")
    public List<CustomerRecord> customers(@RequestParam(required = false, defaultValue = "") String q) {
        if (q.isBlank()) {
            return dataInquiryService.allCustomers();
        }
        return dataInquiryService.searchCustomers(q);
    }

    @PostMapping("/customers/search")
    public List<CustomerRecord> searchCustomers(@Valid @RequestBody DataInquiryRequest request) {
        return dataInquiryService.searchCustomers(request.query());
    }

    @GetMapping("/orders")
    public List<OrderRecord> orders(@RequestParam(required = false, defaultValue = "") String q) {
        if (q.isBlank()) {
            return dataInquiryService.allOrders();
        }
        return dataInquiryService.searchOrders(q);
    }

    @PostMapping("/orders/search")
    public List<OrderRecord> searchOrders(@Valid @RequestBody DataInquiryRequest request) {
        return dataInquiryService.searchOrders(request.query());
    }

    @GetMapping("/products")
    public List<ProductRecord> products(@RequestParam(required = false, defaultValue = "") String q) {
        if (q.isBlank()) {
            return dataInquiryService.allProducts();
        }
        return dataInquiryService.searchProducts(q);
    }

    @PostMapping("/products/search")
    public List<ProductRecord> searchProducts(@Valid @RequestBody DataInquiryRequest request) {
        return dataInquiryService.searchProducts(request.query());
    }
}
