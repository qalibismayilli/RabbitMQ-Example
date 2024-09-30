package com.company.rabbitmqexample.api;

import com.company.rabbitmqexample.dto.AccountResponse;
import com.company.rabbitmqexample.dto.CreateAccountRequest;
import com.company.rabbitmqexample.service.AccountService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(CreateAccountRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    @DeleteMapping("/delete")
    public void deleteAccount(String accountId){
        accountService.deleteAccount(accountId);
    }

    @PostMapping("/withdrawMoney")
    public ResponseEntity<AccountResponse> withdrawMoney(String accountId, Double amount){
        return ResponseEntity.ok(accountService.withdrawMoney(accountId, amount));
    }

    @PutMapping("/transfer")
    public void transferMoney(){
        accountService.transferMoney();
    }
}
