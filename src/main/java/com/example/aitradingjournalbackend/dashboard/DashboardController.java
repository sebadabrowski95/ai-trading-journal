package com.example.aitradingjournalbackend.dashboard;

import com.example.aitradingjournalbackend.dashboard.dto.DashboardRangeRequest;
import com.example.aitradingjournalbackend.dashboard.dto.DashboardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @PostMapping("/charts")
    public DashboardResponse charts(@Valid @RequestBody DashboardRangeRequest request,
                                    Authentication authentication) {
        return dashboardService.getChartsData(request, authentication);
    }
}
