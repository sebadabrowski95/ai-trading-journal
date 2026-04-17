package com.example.aitradingjournalbackend.AI;

import com.example.aitradingjournalbackend.transaction.Transaction;
import com.example.aitradingjournalbackend.transaction.repo.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
public class AiTradingAnalysisService {

    private final TransactionRepository transactionRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    private static final int LAST_7_DAYS = 7;

    public AiTradingAnalysisService(TransactionRepository transactionRepository, ChatClient.Builder builder) {
        this.transactionRepository = transactionRepository;
        this.chatClient = builder.build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String analyzeUserTransactions(Long userId, String userQuestion) {
        Instant sevenDaysAgo = Instant.now().minus(LAST_7_DAYS, ChronoUnit.DAYS);
        
        List<Transaction> transactions = transactionRepository
                .findAllByUserIdAndCloseTimeGreaterThanEqualOrderByCloseTimeDesc(userId, sevenDaysAgo);
        
        if (transactions.isEmpty()) {
            return "Nie znaleziono transakcji z ostatnich " + LAST_7_DAYS + " dni. Dodaj transakcje, aby uzyskać analizę.";
        }

        List<TransactionSummary> summaries = transactions.stream()
                .map(t -> new TransactionSummary(
                        t.getPosition(),
                        t.getSymbol(),
                        t.getType(),
                        t.getVolume(),
                        t.getOpenTime(),
                        t.getOpenPrice(),
                        t.getCloseTime(),
                        t.getClosePrice(),
                        t.getGrossPl(),
                        t.getSl(),
                        t.getTp(),
                        t.getCommission(),
                        t.getSwap(),
                        t.getRollover(),
                        t.getComment()
                ))
                .toList();

        String transactionsJson = toPrettyJson(summaries);

        String systemPrompt = """
            Jesteś profesjonalnym trenerem tradingowym
            Twoja rola to pomaganie traderom w poprawie ich wyników poprzez analizę transakcji, identyfikację błędów psychologicznych i behawioralnych oraz dostarczanie konkretnych zaleceń.
            
            ANALIZA WYMAGANA:
            
            1. **Kluczowe metryki:**
               - Win rate (% wygranych transakcji)
               - Całkowity profit/strata (suma grossPl)
               - Średni zysk na transakcji wygranej i średnia strata na przegranej
               - Risk-Reward ratio (na podstawie SL/TP gdzie dostępne)
               - Łączna liczba transakcji i dni tradingowych
            
            2. **AUTOMATYCZNE WYKRYWANIE BŁĘDÓW TRADINGOWYCH:**
            
               **OVERTRADING (nadmierny trading)** - wykryj gdy:
               - Zbyt mały średni interwał między transakcjami
               - Wysoka liczba transakcji w dniach stratnych
            
               **REVENGE TRADING (odwet za stratę)** - wykryj gdy:
               - Po stracie następuje natychmiastowa kolejna transakcja
               - Zwiększona ekspozycja po serii strat
               - Transakcje bez wyraźnej okazji po emocjonalnej stracie
               - Zmiana strategii/symbolu po stracie

            
               Inne błędy do wykrycia:
               - Zbyt szeroki Stop Loss
               - Przedwczesne zamykanie pozycji
               - Porównaj wynik do zachowania instrumentu danego dnia
               - Trading na zbyt wielu instrumentach jednocześnie
               - Brak konsekwencji w strategii
            
            3. **KONKRETNE WSKAZÓWKI:**
               - Co użytkownik robi dobrze
               - Co wymaga natychmiastowej poprawy
               - 3-5 konkretnych, realistycznych zaleceń na przyszłość
               - Na co zwrócić uwagę w kolejnych dniach
            
            STYL ODPOWIEDZI:
            - Bądź bezpośredni i konkretny
            - Odpowiadaj z empatią, ale nie unikaj trudnych prawd
            - Używaj konkretnych liczb i przykładów z transakcji użytkownika
            - Formatuj odpowiedź czytelnie (używaj emoji, nagłówków, list)
            - Bazuj WYŁĄCZNIE na podanych danych - nie wymyślaj faktów
            - Jeśli wykryjesz overtrading lub revenge trading - POWIADOM O TYM WYKREŚLENIU i wyjaśnij dlaczego
            """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userSpec -> userSpec
                        .text("""
                            Oto transakcje użytkownika z ostatnich {days} dni (posortowane od najnowszej do najstarszej):
                            
                            Statystyki bazy:
                            - Łączna liczba transakcji: {count}
                            - Okres: od {oldest} do {newest}
                            
                            ```json
                            {transactions}
                            ```
                            
                            Pytanie użytkownika: {question}
                            """)
                        .param("days", LAST_7_DAYS)
                        .param("count", transactions.size())
                        .param("oldest", transactions.get(transactions.size() - 1).getCloseTime())
                        .param("newest", transactions.get(0).getCloseTime())
                        .param("transactions", transactionsJson)
                        .param("question", userQuestion != null && !userQuestion.isBlank()
                                ? userQuestion
                                : "Przeanalizuj moje transakcje z ostatnich dni. Szukaj błędów takich jak overtrading i revenge trading. Podaj konkretne wnioski i zalecenia.")
                )
                .call()
                .content();
    }

    private String toPrettyJson(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting transactions to JSON for AI", e);
        }
    }
}