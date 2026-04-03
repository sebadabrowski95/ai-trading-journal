package com.example.aitradingjournalbackend.transaction;

import com.example.aitradingjournalbackend.transaction.dto.CreateTransactionRequest;
import com.example.aitradingjournalbackend.transaction.dto.TransactionCalendarResponse;
import com.example.aitradingjournalbackend.transaction.dto.TransactionDayDetailsResponse;
import com.example.aitradingjournalbackend.transaction.dto.TransactionImportResponse;
import com.example.aitradingjournalbackend.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<TransactionResponse> list(Authentication authentication) {
        return transactionService.findCurrentUserTransactions(authentication);
    }

    @GetMapping("/calendar")
    public TransactionCalendarResponse calendar(@RequestParam int year,
                                                @RequestParam int month,
                                                Authentication authentication) {
        return transactionService.getCalendar(year, month, authentication);
    }

    @GetMapping("/day")
    public TransactionDayDetailsResponse day(@RequestParam LocalDate date, Authentication authentication) {
        return transactionService.getDayDetails(date, authentication);
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable Long id, Authentication authentication) {
        return transactionService.findCurrentUserTransaction(id, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody CreateTransactionRequest request,
                                      Authentication authentication) {
        return transactionService.createTransaction(request, authentication);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionImportResponse importExcel(@RequestParam("file") MultipartFile file,
                                                 Authentication authentication) {
        return transactionService.importTransactions(file, authentication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        transactionService.deleteCurrentUserTransaction(id, authentication);
    }
}
