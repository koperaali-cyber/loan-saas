package com.loansaas.service;

import com.loansaas.entity.Loan;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ---------------- PDF ----------------
    public byte[] loansToPdf(String businessName, List<Loan> loans) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
            PdfWriter.getInstance(document, out);
            document.open();

            com.lowagie.text.Font titleFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(30, 41, 59));
            com.lowagie.text.Font subFont =
                    FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));

            Paragraph title = new Paragraph("Loan Report", titleFont);
            document.add(title);
            Paragraph sub = new Paragraph(businessName + "  |  Generated: "
                    + java.time.LocalDate.now().format(DF), subFont);
            sub.setSpacingAfter(12);
            document.add(sub);

            PdfPTable table = new PdfPTable(new float[]{1.2f, 3f, 2f, 2f, 2f, 2f, 2f});
            table.setWidthPercentage(100);

            String[] headers = {"ID", "Customer", "Amount", "Total Due", "Paid", "Balance", "Status"};
            com.lowagie.text.Font headFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                cell.setBackgroundColor(new Color(37, 99, 235));
                cell.setPadding(6);
                table.addCell(cell);
            }

            com.lowagie.text.Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (Loan l : loans) {
                table.addCell(cell("#" + l.getId(), bodyFont));
                table.addCell(cell(l.getCustomer().getFullName(), bodyFont));
                table.addCell(cell(fmt(l.getAmount()), bodyFont));
                table.addCell(cell(fmt(l.getTotalRepayment()), bodyFont));
                table.addCell(cell(fmt(l.getAmountPaid()), bodyFont));
                table.addCell(cell(fmt(l.getRemainingBalance()), bodyFont));
                table.addCell(cell(l.getStatus().name(), bodyFont));
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build PDF: " + e.getMessage(), e);
        }
    }

    private PdfPCell cell(String text, com.lowagie.text.Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(5);
        return c;
    }

    private String fmt(java.math.BigDecimal v) {
        if (v == null) return "0";
        return String.format("%,.0f", v);
    }

    // ---------------- Excel ----------------
    public byte[] loansToExcel(String businessName, List<Loan> loans) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Loans");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font hFont = workbook.createFont();
            hFont.setBold(true);
            hFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(hFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Loan Report - " + businessName);

            Row header = sheet.createRow(2);
            String[] cols = {"ID", "Customer", "Phone", "Amount", "Interest %",
                    "Total Due", "Paid", "Balance", "Start", "Due", "Status"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            int r = 3;
            for (Loan l : loans) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(l.getId());
                row.createCell(1).setCellValue(l.getCustomer().getFullName());
                row.createCell(2).setCellValue(l.getCustomer().getPhone());
                row.createCell(3).setCellValue(l.getAmount().doubleValue());
                row.createCell(4).setCellValue(l.getInterestRate().doubleValue());
                row.createCell(5).setCellValue(l.getTotalRepayment().doubleValue());
                row.createCell(6).setCellValue(l.getAmountPaid().doubleValue());
                row.createCell(7).setCellValue(l.getRemainingBalance().doubleValue());
                row.createCell(8).setCellValue(l.getStartDate().format(DF));
                row.createCell(9).setCellValue(l.getDueDate().format(DF));
                row.createCell(10).setCellValue(l.getStatus().name());
            }
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Excel: " + e.getMessage(), e);
        }
    }
}