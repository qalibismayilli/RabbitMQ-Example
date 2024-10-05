package com.company.rabbitmqexample.service;

import com.company.rabbitmqexample.dto.AccountResponse;
import com.company.rabbitmqexample.dto.CreateAccountRequest;
import com.company.rabbitmqexample.dto.MoneyTransferRequest;
import com.company.rabbitmqexample.model.Account;
import com.company.rabbitmqexample.model.Currency;
import com.company.rabbitmqexample.model.Customer;
import com.company.rabbitmqexample.repository.AccountRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Optional;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerService customerService;

    private final DirectExchange exchange;
    private final AmqpTemplate amqpTemplate;

    @Value("${sample.rabbitmq.routingKey}")
    String routingKey;

    @Value("${sample.rabbitmq.queue}")
    String queueName;


    public AccountService(AccountRepository accountRepository, CustomerService customerService,
                          DirectExchange exchange, AmqpTemplate rabbitTemplate) {
        this.accountRepository = accountRepository;
        this.customerService = customerService;
        this.exchange = exchange;
        this.amqpTemplate = rabbitTemplate;
    }

    private AccountResponse convertToResponse(Account account) {
        return new AccountResponse(account.getId(),
                account.getCustomer().getId(),
                account.getBalance().doubleValue(),
                account.getCurrency());
    }

    @Transactional
    @CachePut(value = "accounts", key = "#id")
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

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public void deleteAccount(String accountId) {
        accountRepository.deleteById(accountId);
    }

    @Transactional
    public AccountResponse addMoney(String accountId, double amount) {
        Account account = accountRepository
                .findById(accountId).orElseThrow(() -> new RuntimeException("Account Not Found"));

        account.setBalance(account.getBalance() + amount);

        return convertToResponse(accountRepository.save(account));
    }

    @Transactional
    public void transferMoney(MoneyTransferRequest request) {
        amqpTemplate.convertAndSend(exchange.getName(), routingKey, request);
    }

    @Transactional
    @RabbitListener(queues = "${sample.rabbitmq.queue}")
    public void transferMoneyMessage(MoneyTransferRequest request) {
        Account fromAccount = accountRepository
                .findById(request.getFromId()).orElseThrow(() -> new RuntimeException("fromAccount Not Found"));
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("fromAccount Balance is less than amount");
        } else {
            fromAccount.setBalance(fromAccount.getBalance() - request.getAmount());
            accountRepository.save(fromAccount);
            amqpTemplate.convertAndSend(exchange.getName(), "secondRoute", request);
        }
    }

    @Transactional
    @RabbitListener(queues = "secondStepQueue")
    public void updateReceiverAccount(MoneyTransferRequest request) {
        Optional<Account> toAccount = accountRepository.findById(request.getToId());

        toAccount.ifPresentOrElse(account -> {
            account.setBalance(account.getBalance() + request.getAmount());
            accountRepository.save(account);
            amqpTemplate.convertAndSend(exchange.getName(), "thirdRoute", request);
        }, () -> {
            Optional<Account> fromAccount = accountRepository.findById(request.getToId());
            fromAccount.ifPresent(account -> {
                System.out.println("Money charge back to sender");
                account.setBalance(account.getBalance() + request.getAmount());
                accountRepository.save(account);
            });
            throw new RuntimeException("Account Not Found");
        });
    }

    @Transactional
    @RabbitListener(queues = "thirdStepQueue")
    public void finalizeTransferMoney(MoneyTransferRequest request) {
        Optional<Account> fromAccount = accountRepository.findById(request.getFromId());
        fromAccount.ifPresentOrElse(account ->
                        System.out.println("Sender(" + account.getId() +
                                ") new account balance: " + account.getBalance()),
                () -> new RuntimeException("Sender not found"));

        Optional<Account> toAccount = accountRepository.findById(request.getToId());
        toAccount.ifPresentOrElse(account ->
                        System.out.println("Receiver(" + account.getId() +
                                ") new account balance: " + account.getBalance()),
                () -> new RuntimeException("Sender not found"));

    }

    @Transactional
    public AccountResponse withdrawMoney(String accountId, Double amount) {

        Account account = accountRepository.
                findById(accountId).
                orElseThrow(() -> new RuntimeException("Account Not Found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient Funds!");
        } else {
            account.setBalance(account.getBalance() - amount);
            return convertToResponse(accountRepository.save(account));
        }
    }
}