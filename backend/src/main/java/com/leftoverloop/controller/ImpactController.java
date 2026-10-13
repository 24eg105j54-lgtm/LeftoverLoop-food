package com.leftoverloop.controller;

import com.leftoverloop.dto.ImpactStatsDto;
import com.leftoverloop.service.ImpactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/impact")
public class ImpactController {

    @Autowired
    private ImpactService impactService;

    @GetMapping("/public/stats")
    public ResponseEntity<ImpactStatsDto> getImpactStats() {
        return ResponseEntity.ok(impactService.getImpactStats());
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportImpactCsv() {
        String csvContent = impactService.generateImpactCsv();
        byte[] bytes = csvContent.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "leftoverloop_impact_report.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}
