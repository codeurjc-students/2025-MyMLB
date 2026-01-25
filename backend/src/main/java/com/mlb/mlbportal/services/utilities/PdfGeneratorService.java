package com.mlb.mlbportal.services.utilities;

import com.mlb.mlbportal.models.ticket.Ticket;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGeneratorService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    public byte[] generateTicketsPdf(List<Ticket> tickets) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            for (Ticket ticket : tickets) {
                String formattedDate = ticket.getEventManager().getEvent().getMatch().getDate().format(DATE_FORMATTER);

                document.add(new Paragraph("MLB PORTAL - OFFICIAL TICKETS", titleFont));
                document.add(new Paragraph("--------------------------------------------------"));
                document.add(new Paragraph("Ticket ID: #" + ticket.getId(), boldFont));
                document.add(new Paragraph("Match: " + ticket.getEventManager().getEvent().getMatch().getAwayTeam().getName() +
                        " @ " + ticket.getEventManager().getEvent().getMatch().getHomeTeam().getName(), bodyFont));
                document.add(new Paragraph("Date: " + formattedDate, bodyFont));
                document.add(new Paragraph("Stadium: " + ticket.getEventManager().getEvent().getStadium().getName(), bodyFont));
                document.add(new Paragraph("Sector: " + ticket.getSeat().getSector().getName(), bodyFont));
                document.add(new Paragraph("Seat: " + ticket.getSeat().getName(), bodyFont));
                document.add(new Paragraph("Owner: " + ticket.getOwnerName(), bodyFont));
                document.add(new Paragraph("\n\n"));
            }

            document.close();
        }
        catch (DocumentException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}