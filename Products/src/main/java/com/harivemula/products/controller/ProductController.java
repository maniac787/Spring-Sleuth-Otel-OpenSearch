package com.harivemula.products.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @RequestMapping("/products")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello From Products!");
    }

    @RequestMapping("/products/customer/{customerId}")
    public ResponseEntity<String> getCustomerProducts(@PathVariable String customerId) {
        return ResponseEntity.ok(String.format("CustomerId: %s; Products-A,B,C,D", customerId));
    }

}
