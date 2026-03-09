package com.mlb.mlbportal.services.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.mlb.mlbportal.models.ticket.Ticket;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfGeneratorService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy - HH:mm");
    private static final String LOGO_PATH = "/static/assets/logo.png";

    // Colors
    private static final BaseColor BLUE = new BaseColor(0, 33, 71);
    private static final BaseColor RED = new BaseColor(186, 12, 47);
    private static final BaseColor LIGHT_GRAY = new BaseColor(240, 240, 240);

    public byte[] generateTicketsPdf(List<Ticket> tickets) {
        Document document = new Document(PageSize.A4, 36, 36, 90, 54);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            // Header Layout
            writer.setPageEvent(new HeaderLayout());

            document.open();

            // Cover Page
            this.generateCoverPage(document);

            // Display Tickets Information
            for (int i = 0; i < tickets.size(); i++) {
                this.addTicketContent(document, tickets.get(i));
                if (i < tickets.size() - 1) {
                    document.newPage();
                }
            }
            document.close();
        }
        catch (Exception e) {
            log.error("Error generating the PDF: {}", e.getMessage());
        }
        return out.toByteArray();
    }

    /**
     * Generates the cover page for the PDF file.
     *
     * @param pdf The active Document instance.
     * @throws Exception if image loading or element addition fails.
     */
    private void generateCoverPage(Document pdf) throws Exception {
        this.addEmptyLine(pdf, 3);
        try {
            URL imageUrl = getClass().getResource(LOGO_PATH);
            if (imageUrl != null) {
                Image logo = Image.getInstance(imageUrl);
                logo.scaleToFit(200, 200);
                logo.setAlignment(Element.ALIGN_CENTER);
                pdf.add(logo);
            }
        }
        catch (DocumentException | IOException e) {
            log.warn("Could not load cover image: {}", e.getMessage());
        }
        this.addEmptyLine(pdf, 2);

        // Main Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 40, BLUE);
        Paragraph title = new Paragraph("MLB PORTAL", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        pdf.add(title);

        // Subtitle
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 18, RED);
        Paragraph subtitle = new Paragraph("DIGITAL TICKETS", subFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        pdf.add(subtitle);
        this.addEmptyLine(pdf, 2);

        Paragraph line = new Paragraph("________________________________________________", subFont);
        line.setAlignment(Element.ALIGN_CENTER);
        pdf.add(line);

        this.addEmptyLine(pdf, 8);
        pdf.newPage();
    }

    /**
     * Render the ticket information in the pdf file.
     *
     * @param document The active Document instance.
     * @param ticket The ticket to render.
     * @throws DocumentException if table positioning or addition fails.
     */
    private void addTicketContent(Document document, Ticket ticket) throws DocumentException {
        // Main Table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        // Event Title
        String matchName = ticket.getEventManager().getEvent().getMatch().getAwayTeam().getName() +
                " @ " +
                ticket.getEventManager().getEvent().getMatch().getHomeTeam().getName();

        PdfPCell eventCell = new PdfPCell(new Phrase(matchName.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.WHITE)));
        eventCell.setBackgroundColor(BLUE);
        eventCell.setPadding(12);
        eventCell.setColspan(2);
        eventCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        eventCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(eventCell);

        // Ticket Details
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.DARK_GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

        this.addInfoRow(table, "DATE & TIME", ticket.getEventManager().getEvent().getMatch().getDate().format(DATE_FORMATTER), labelFont, valueFont);
        this.addInfoRow(table, "STADIUM", ticket.getEventManager().getEvent().getMatch().getStadium().getName(), labelFont, valueFont);
        this.addInfoRow(table, "LOCATION", "Sector: " + ticket.getSeat().getSector().getName() + " | Seat: " + ticket.getSeat().getName(), labelFont, valueFont);
        this.addInfoRow(table, "HOLDER", ticket.getOwnerName(), labelFont, valueFont);

        document.add(table);
    }

    /**
     * Add a row to the table following the (key, value) structure.
     *
     * @param table The target PdfPTable.
     * @param label The category name.
     * @param value The actual data content.
     * @param lFont Font for the label.
     * @param vFont Font for the value.
     */
    private void addInfoRow(PdfPTable table, String label, String value, Font lFont, Font vFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, lFont));
        cellLabel.setPadding(10);
        cellLabel.setBackgroundColor(LIGHT_GRAY);
        cellLabel.setBorderColor(BaseColor.WHITE);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, vFont));
        cellValue.setPadding(10);
        cellValue.setBorderColor(BaseColor.WHITE);
        table.addCell(cellValue);
    }

    private void addEmptyLine(Document document, int number) throws DocumentException {
        for (int i = 0; i < number; i++) {
            document.add(new Paragraph(" "));
        }
    }

    /**
     * Inner class to manage Header, Footer line and Page Numbers
     */
    @Slf4j
    private static class HeaderLayout extends PdfPageEventHelper {
        private Image headerLogo;

        public HeaderLayout() {
            try {
                URL imageUrl = PdfGeneratorService.class.getResource(LOGO_PATH);
                if (imageUrl != null) {
                    this.headerLogo = Image.getInstance(imageUrl);
                    this.headerLogo.scaleToFit(30, 30);
                }
            }
            catch (BadElementException | IOException ex) {
                log.error("Error rendering the logo in the header: {}", ex.getMessage());
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            // Skip the cover page
            if (writer.getPageNumber() != 1) {
                PdfContentByte cb = writer.getDirectContent();
                try {
                    if (this.headerLogo != null) {
                        this.headerLogo.setAbsolutePosition(36, writer.getPageSize().getTop() - 45);
                        cb.addImage(this.headerLogo);
                    }

                    // Text align to the right
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                            new Phrase("TICKETS - MLB PORTAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BLUE)),
                            writer.getPageSize().getRight() - 36, writer.getPageSize().getTop() - 35, 0);

                    // Horizontal delimiter
                    cb.setLineWidth(1f);
                    cb.setRGBColorStrokeF(0.8f, 0.8f, 0.8f);
                    cb.moveTo(36, writer.getPageSize().getTop() - 55);
                    cb.lineTo(writer.getPageSize().getRight() - 36, writer.getPageSize().getTop() - 55);
                    cb.stroke();

                    // Page Number
                    String pageNum = String.valueOf(writer.getPageNumber() - 1);
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            new Phrase("Page " + pageNum, FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY)),
                            (writer.getPageSize().getLeft() + writer.getPageSize().getRight()) / 2, 25, 0);

                }
                catch (DocumentException ex) {
                    log.error("Error in the Header Layout: {}", ex.getMessage());
                }
            }
        }
    }
}