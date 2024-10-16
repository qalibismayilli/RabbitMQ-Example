package com.company.rabbitmqexample.dto;

import com.company.rabbitmqexample.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AccountResponse implements Serializable {

    private String id;
    private String customerId;
    private Double balance;
    private Currency currency;


}
