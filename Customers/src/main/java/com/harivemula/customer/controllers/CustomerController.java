package com.harivemula.customer.controllers;

import com.harivemula.customer.dto.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.ContinueSpan;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
public class CustomerController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String PRODUCTS_URL = "http://localhost:8081/products/customer/{id}";

    @GetMapping("/customers")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from Customers!");
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<String> getCustomerDetails(@PathVariable String customerId) {
        return ResponseEntity.ok("CustomerId: "+customerId+"; Name: Static User");
    }


    @GetMapping("/customers/products/{customerId}")
    public ResponseEntity<String> getCustomerProducts (@PathVariable String customerId) {
        String products = restTemplate.getForObject(PRODUCTS_URL, String.class, customerId);
        log.info("Products:["+products+"]");
        return ResponseEntity.ok(products);
    }

    @PostMapping("/customers")
    public ResponseEntity<String> createCustomer(@RequestBody Customer customer) {
        log.info("Customer:["+customer.getName()+"]; Id:["+customer.getId()+"]");
        return ResponseEntity.status(HttpStatus.CREATED).body("Customer Created");
    }


}
