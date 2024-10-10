package com.company.rabbitmqexample.config;

import com.company.rabbitmqexample.dto.CreateAccountRequest;
import com.company.rabbitmqexample.model.Account;
import com.company.rabbitmqexample.model.City;
import com.company.rabbitmqexample.model.Currency;
import com.company.rabbitmqexample.model.Customer;
import com.company.rabbitmqexample.repository.CustomerRepository;
import com.company.rabbitmqexample.service.AccountService;
import com.company.rabbitmqexample.service.CustomerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class StartupConfig implements CommandLineRunner {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public StartupConfig(AccountService accountService, CustomerService customerService, CustomerRepository customerRepository) {
        this.accountService = accountService;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }


    @Override
    public void run(String... args) throws Exception {
        Customer c1 = Customer.builder()
                .name("James")
                .city(City.BARCELONA)
                .dateOfBirth(1988)
                .build();


        Customer c2 = Customer.builder()
                .name("Alex")
                .city(City.BILBAO)
                .dateOfBirth(2000)
                .build();

        Customer c3 = Customer.builder()
                .name("Pique")
                .city(City.MADRID)
                .dateOfBirth(2005)
                .build();

        customerRepository.saveAll(Arrays.asList(c1,c2,c3));


        accountService.createAccount(CreateAccountRequest.builder()
                .customerId(c1.getId())
                .balance(1320.0)
                .currency(Currency.USD)
                .build());

        accountService.createAccount(CreateAccountRequest.builder()
                .customerId(c2.getId())
                .balance(7898.0)
                .currency(Currency.USD)
                .build());
        accountService.createAccount(CreateAccountRequest.builder()
                .customerId(c3.getId())
                .balance(120000.0)
                .currency(Currency.USD)
                .build());


    }
}