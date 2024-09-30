package com.company.rabbitmqexample.service;

import com.company.rabbitmqexample.dto.AccountResponse;
import com.company.rabbitmqexample.dto.CreateAccountRequest;
import com.company.rabbitmqexample.model.Account;
import com.company.rabbitmqexample.model.Currency;
import com.company.rabbitmqexample.model.Customer;
import com.company.rabbitmqexample.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerService customerService;

    public AccountService(AccountRepository accountRepository, CustomerService customerService) {
        this.accountRepository = accountRepository;
        this.customerService = customerService;
    }

    @Transactional
    public AccountResponse createAccount(@NotNull CreateAccountRequest request) {
        Customer customer = customerService.findCustomerById(request.getCustomerId());

        if (customer.getId() == null || customer.getId().trim().equals("")) {
            throw new RuntimeException("Customer Not Found!");
        }

        Account account = Account.builder()
                .city(customer.getCity())
                .balance(new BigDecimal(0))
                .currency(Currency.USD)
                .customer(customer)
                .build();

        accountRepository.save(account);
        return new AccountResponse(account.getId(),
                account.getCustomer().getId(),
                account.getBalance().doubleValue(),
                account.getCurrency());
    }
}
