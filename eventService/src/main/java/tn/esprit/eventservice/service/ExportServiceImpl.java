package tn.esprit.eventservice.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.Participant;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.EventRepository;
import tn.esprit.eventservice.repository.ParticipantRepository;

import java.io.IOException;
import java.util.List;

@Service
public class ExportServiceImpl implements IExportService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;

    public ExportServiceImpl(ParticipantRepository participantRepository,
                             EventRepository eventRepository) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void exportParticipantsToPDF(Long eventId, HttpServletResponse response) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable : " + eventId));

        List<Participant> participants = participantRepository.findByEventId(eventId);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=participants_event_" + eventId + ".pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Titre
        document.add(new Paragraph("Liste des Participants")
                .setFontSize(20)
                .setBold()
                .setFontColor(ColorConstants.DARK_GRAY));

        document.add(new Paragraph("Événement : " + event.getName())
                .setFontSize(14)
                .setFontColor(ColorConstants.GRAY));



        document.add(new Paragraph("Total : " + participants.size() + " participant(s)")
                .setFontSize(12)
                .setMarginBottom(20));

        // Tableau
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 4, 2, 3}))
                .useAllAvailableWidth();

        // En-têtes
        String[] headers = {"#", "Nom complet", "Email", "Téléphone", "Date inscription"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setBold())
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setFontColor(ColorConstants.WHITE));
        }

        // Données
        for (int i = 0; i < participants.size(); i++) {
            Participant p = participants.get(i);
            table.addCell(String.valueOf(i + 1));
            table.addCell(p.getFullName());
            table.addCell(p.getEmail());
            table.addCell(p.getPhone() != null ? p.getPhone() : "-");
            table.addCell(p.getRegistrationDate() != null
                    ? p.getRegistrationDate().toString() : "-");
        }

        document.add(table);
        document.close();
    }
}