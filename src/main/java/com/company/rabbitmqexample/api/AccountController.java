package com.company.rabbitmqexample.api;

import com.company.rabbitmqexample.dto.AccountResponse;
import com.company.rabbitmqexample.dto.CreateAccountRequest;
import com.company.rabbitmqexample.dto.MoneyTransferRequest;
import com.company.rabbitmqexample.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    @DeleteMapping("/delete")
    public void deleteAccount(@RequestParam String accountId){
        accountService.deleteAccount(accountId);
    }

    @PutMapping("/withdrawMoney")
    public ResponseEntity<AccountResponse> withdrawMoney(@RequestParam String accountId,@RequestParam Double amount){
        return ResponseEntity.ok(accountService.withdrawMoney(accountId, amount));
    }

    @PutMapping("/transfer")
    public ResponseEntity<Void> transferMoney(@RequestBody MoneyTransferRequest request){
        accountService.transferMoney(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/addMoney")
    public ResponseEntity<AccountResponse> addMoney(@RequestParam String accountId, @RequestParam Double amount){
        return ResponseEntity.ok(accountService.addMoney(accountId, amount));
    }

    @GetMapping("/getAccount")
    public ResponseEntity<AccountResponse> getAccountById(@RequestParam String accountId){
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccounts(){
        return ResponseEntity.ok(accountService.getAllAccounts());
    }
}
