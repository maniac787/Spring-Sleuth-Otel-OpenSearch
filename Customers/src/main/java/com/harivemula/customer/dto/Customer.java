package com.harivemula.customer.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Customer {
    private String name;
    private String id;
}
