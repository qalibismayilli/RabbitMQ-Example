package com.company.rabbitmqexample.service;

import com.company.rabbitmqexample.dto.AccountResponse;
import com.company.rabbitmqexample.dto.CreateAccountRequest;
import com.company.rabbitmqexample.model.Account;
import com.company.rabbitmqexample.model.Currency;
import com.company.rabbitmqexample.model.Customer;
import com.company.rabbitmqexample.repository.AccountRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerService customerService;

    private final AmqpTemplate rabbitTemplate;

    @Value("${sample.rabbitmq.routingKey}")
    String routingKey;

    @Value("${sample.rabbitmq.queue}")
    String queueName;


    public AccountService(AccountRepository accountRepository, CustomerService customerService, AmqpTemplate rabbitTemplate) {
        this.accountRepository = accountRepository;
        this.customerService = customerService;
        this.rabbitTemplate = rabbitTemplate;
    }

    private AccountResponse convertToResponse(Account account){
        return new AccountResponse(account.getId(),
                account.getCustomer().getId(),
                account.getBalance().doubleValue(),
                account.getCurrency());
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse createAccount(@NotNull CreateAccountRequest request) {
        Customer customer = customerService.findCustomerById(request.getCustomerId());

        if (customer.getId() == null || customer.getId().trim().equals("")) {
            throw new RuntimeException("Customer Not Found!");
        }

        Account account = Account.builder()
                .city(customer.getCity())
                .balance(0.0)
                .currency(Currency.USD)
                .customer(customer)
                .build();

        accountRepository.save(account);
        return convertToResponse(account);
    }

    @CacheEvict(value = "accounts", allEntries = true)
    public void deleteAccount(String accountId){
        accountRepository.deleteById(accountId);
    }

    public void transferMoney(){

    }

    public AccountResponse withdrawMoney(String accountId, Double amount){

        Account account =  accountRepository.
                findById(accountId).
                orElseThrow(() -> new RuntimeException("Account Not Found"));

        if(account.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient Funds!");
        }else{
            account.setBalance(account.getBalance()-amount);
            return convertToResponse(accountRepository.save(account));
        }
    }
}
