package tn.esprit.eventservice.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IExportService {
    void exportParticipantsToPDF(Long eventId, HttpServletResponse response) throws IOException;
}