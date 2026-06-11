package tn.esprit.eventservice.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventservice.service.IExportService;

import java.io.IOException;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final IExportService exportService;

    public ExportController(IExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/participants/{eventId}/pdf")
    public void exportPDF(@PathVariable Long eventId,
                          HttpServletResponse response) throws IOException {
        exportService.exportParticipantsToPDF(eventId, response);
    }
}