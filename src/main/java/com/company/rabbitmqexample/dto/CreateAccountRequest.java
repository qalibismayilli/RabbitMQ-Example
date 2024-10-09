package com.company.rabbitmqexample.dto;

import com.company.rabbitmqexample.model.Currency;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class CreateAccountRequest {
    @NotBlank(message = "Account id must not be empty")
    private String id;
    private String customerId;
    private Double balance;
    private Currency currency;
}
