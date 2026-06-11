package tn.esprit.subspaymentservice.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import tn.esprit.subspaymentservice.entity.Payment;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

@Service
public class InvoiceService {

    public byte[] generateInvoicePdf(Payment payment) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("CoCo SaaS - FACTURE").setFontSize(18).setBold());
            document.add(new Paragraph("--------------------------------------------------"));

            document.add(new Paragraph("Informations Client :"));
            document.add(new Paragraph("Utilisateur ID : #" + payment.getUserId()));
            document.add(new Paragraph("Date : " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(payment.getCreatedAt())));

            document.add(new Paragraph("\n"));

            Table table = new Table(UnitValue.createPointArray(new float[]{200, 100, 100}));
            table.addHeaderCell("Description");
            table.addHeaderCell("Plan");
            table.addHeaderCell("Total");

            table.addCell("Abonnement CoCo");
            table.addCell(payment.getSubscription().getPlan().getName());
            table.addCell(payment.getAmount() + " " + payment.getCurrency());

            document.add(table);

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Statut : " + payment.getStatus()));
            document.add(new Paragraph("ID Transaction : " + payment.getStripePaymentId()));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Glory to ESPRIT!").setItalic());

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
}
