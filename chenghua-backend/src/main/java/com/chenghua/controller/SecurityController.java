package com.chenghua.controller;

import com.chenghua.security.CrawlerLogEntry;
import com.chenghua.security.CrawlerLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final CrawlerLogService crawlerLogService;

    public SecurityController(CrawlerLogService crawlerLogService) {
        this.crawlerLogService = crawlerLogService;
    }

    @GetMapping("/crawler-logs")
    public List<CrawlerLogEntry> getCrawlerLogs() {
        return crawlerLogService.getRecentLogs();
    }
}

