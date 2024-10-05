package com.company.rabbitmqexample.dto;

import lombok.Data;

@Data
public class MoneyTransferRequest {
    private String fromId;
    private String toId;
    private Double amount;
}
