package com.example.aitradingjournalbackend.AI;

import com.example.aitradingjournalbackend.AI.dto.AiRequest;
import com.example.aitradingjournalbackend.AI.dto.AiResponse;
import com.example.aitradingjournalbackend.auth.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiTradingAnalysisController {

    private final AiTradingAnalysisService aiTradingAnalysisService;

    @PostMapping("/chat")
    public AiResponse chat(@RequestBody(required = false) AiRequest request,
                          Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String userQuestion = (request != null && request.prompt() != null) 
                ? request.prompt() 
                : null;
        
        String response = aiTradingAnalysisService.analyzeUserTransactions(
                userDetails.getUserId(), 
                userQuestion
        );
        
        return new AiResponse(response);
    }
}
