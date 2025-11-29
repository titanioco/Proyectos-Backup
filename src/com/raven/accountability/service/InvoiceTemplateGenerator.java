package com.raven.accountability.service;

import com.raven.accountability.model.Invoice;
import com.raven.accountability.model.InvoiceItem;
import java.io.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Invoice Template Generator for Business Accountability System
 * Generates professional invoice documents in various formats
 */
public class InvoiceTemplateGenerator {
    
    private static final String COMPANY_NAME = "Your Company Name";
    private static final String COMPANY_ADDRESS = "123 Business Street\nCity, State 12345\nCountry";
    private static final String COMPANY_PHONE = "+1 (555) 123-4567";
    private static final String COMPANY_EMAIL = "info@yourcompany.com";
    private static final String COMPANY_WEBSITE = "www.yourcompany.com";
    
    private NumberFormat currencyFormatter;
    private DateTimeFormatter dateFormatter;
    
    public InvoiceTemplateGenerator() {
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        this.dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    }
    
    /**
     * Generate HTML invoice template
     */
    public String generateHTMLInvoice(Invoice invoice) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <title>Invoice ").append(invoice.getInvoiceNumber()).append("</title>\n");
        html.append("    <style>\n");
        html.append(getInvoiceCSS());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Invoice container
        html.append("    <div class='invoice-container'>\n");
        
        // Header
        html.append("        <div class='invoice-header'>\n");
        html.append("            <div class='company-info'>\n");
        html.append("                <h1>").append(COMPANY_NAME).append("</h1>\n");
        html.append("                <div class='company-details'>\n");
        html.append("                    <p>").append(COMPANY_ADDRESS.replace("\n", "<br>")).append("</p>\n");
        html.append("                    <p>Phone: ").append(COMPANY_PHONE).append("</p>\n");
        html.append("                    <p>Email: ").append(COMPANY_EMAIL).append("</p>\n");
        html.append("                    <p>Web: ").append(COMPANY_WEBSITE).append("</p>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            <div class='invoice-title'>\n");
        html.append("                <h2>INVOICE</h2>\n");
        html.append("                <div class='invoice-number'>").append(invoice.getInvoiceNumber()).append("</div>\n");
        html.append("                <div class='invoice-status status-").append(invoice.getStatus().name().toLowerCase()).append("'>\n");
        html.append("                    ").append(invoice.getStatus().getDisplayName()).append("\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        // Invoice details
        html.append("        <div class='invoice-details'>\n");
        html.append("            <div class='customer-info'>\n");
        html.append("                <h3>Bill To:</h3>\n");
        html.append("                <div class='customer-details'>\n");
        html.append("                    <p class='company-name'>").append(invoice.getCustomer().getCompanyName()).append("</p>\n");
        if (invoice.getCustomer().getContactPerson() != null) {
            html.append("                    <p>Attn: ").append(invoice.getCustomer().getContactPerson()).append("</p>\n");
        }
        html.append("                    <p>").append(invoice.getCustomer().getFullAddress()).append("</p>\n");
        if (invoice.getCustomer().getEmail() != null) {
            html.append("                    <p>Email: ").append(invoice.getCustomer().getEmail()).append("</p>\n");
        }
        if (invoice.getCustomer().getPhone() != null) {
            html.append("                    <p>Phone: ").append(invoice.getCustomer().getPhone()).append("</p>\n");
        }
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            <div class='invoice-meta'>\n");
        html.append("                <table>\n");
        html.append("                    <tr><td>Invoice Date:</td><td>").append(invoice.getInvoiceDate().format(dateFormatter)).append("</td></tr>\n");
        html.append("                    <tr><td>Due Date:</td><td>").append(invoice.getDueDate().format(dateFormatter)).append("</td></tr>\n");
        html.append("                    <tr><td>Payment Terms:</td><td>").append(invoice.getCustomer().getPaymentTerms().name().replace("_", " ")).append("</td></tr>\n");
        html.append("                    <tr><td>Currency:</td><td>").append(invoice.getCurrency()).append("</td></tr>\n");
        html.append("                </table>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        // Invoice items table
        html.append("        <div class='invoice-items'>\n");
        html.append("            <table class='items-table'>\n");
        html.append("                <thead>\n");
        html.append("                    <tr>\n");
        html.append("                        <th>Description</th>\n");
        html.append("                        <th>Qty</th>\n");
        html.append("                        <th>Unit</th>\n");
        html.append("                        <th>Unit Price</th>\n");
        html.append("                        <th>Discount</th>\n");
        html.append("                        <th>Total</th>\n");
        html.append("                    </tr>\n");
        html.append("                </thead>\n");
        html.append("                <tbody>\n");
        
        for (InvoiceItem item : invoice.getItems()) {
            html.append("                    <tr>\n");
            html.append("                        <td class='description'>\n");
            html.append("                            <div class='item-description'>").append(item.getDescription()).append("</div>\n");
            if (item.getProductCode() != null && !item.getProductCode().trim().isEmpty()) {
                html.append("                            <div class='product-code'>Code: ").append(item.getProductCode()).append("</div>\n");
            }
            html.append("                        </td>\n");
            html.append("                        <td class='quantity'>").append(item.getFormattedQuantity()).append("</td>\n");
            html.append("                        <td class='unit'>").append(item.getUnit()).append("</td>\n");
            html.append("                        <td class='unit-price'>").append(currencyFormatter.format(item.getUnitPrice())).append("</td>\n");
            html.append("                        <td class='discount'>").append(currencyFormatter.format(item.getDiscountAmount())).append("</td>\n");
            html.append("                        <td class='total'>").append(currencyFormatter.format(item.getTotal())).append("</td>\n");
            html.append("                    </tr>\n");
        }
        
        html.append("                </tbody>\n");
        html.append("            </table>\n");
        html.append("        </div>\n");
        
        // Invoice totals
        html.append("        <div class='invoice-totals'>\n");
        html.append("            <table class='totals-table'>\n");
        html.append("                <tr>\n");
        html.append("                    <td>Subtotal:</td>\n");
        html.append("                    <td>").append(currencyFormatter.format(invoice.getSubtotal())).append("</td>\n");
        html.append("                </tr>\n");
        
        if (invoice.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            html.append("                <tr>\n");
            html.append("                    <td>Discount:</td>\n");
            html.append("                    <td class='discount'>-").append(currencyFormatter.format(invoice.getDiscountAmount())).append("</td>\n");
            html.append("                </tr>\n");
        }
        
        html.append("                <tr>\n");
        html.append("                    <td>Tax:</td>\n");
        html.append("                    <td>").append(currencyFormatter.format(invoice.getTaxAmount())).append("</td>\n");
        html.append("                </tr>\n");
        html.append("                <tr class='total-row'>\n");
        html.append("                    <td><strong>Total Amount:</strong></td>\n");
        html.append("                    <td><strong>").append(currencyFormatter.format(invoice.getTotalAmount())).append("</strong></td>\n");
        html.append("                </tr>\n");
        
        if (invoice.getPaidAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            html.append("                <tr>\n");
            html.append("                    <td>Amount Paid:</td>\n");
            html.append("                    <td class='paid'>").append(currencyFormatter.format(invoice.getPaidAmount())).append("</td>\n");
            html.append("                </tr>\n");
            html.append("                <tr class='balance-row'>\n");
            html.append("                    <td><strong>Balance Due:</strong></td>\n");
            html.append("                    <td><strong>").append(currencyFormatter.format(invoice.getBalanceAmount())).append("</strong></td>\n");
            html.append("                </tr>\n");
        }
        
        html.append("            </table>\n");
        html.append("        </div>\n");
        
        // Notes section
        if (invoice.getNotes() != null && !invoice.getNotes().trim().isEmpty()) {
            html.append("        <div class='invoice-notes'>\n");
            html.append("            <h3>Notes:</h3>\n");
            html.append("            <p>").append(invoice.getNotes().replace("\n", "<br>")).append("</p>\n");
            html.append("        </div>\n");
        }
        
        // Footer
        html.append("        <div class='invoice-footer'>\n");
        html.append("            <div class='payment-info'>\n");
        html.append("                <h3>Payment Information</h3>\n");
        html.append("                <p>Please make payment by the due date. Late payments may incur additional charges.</p>\n");
        html.append("                <p>For questions about this invoice, please contact us at ").append(COMPANY_EMAIL).append("</p>\n");
        html.append("            </div>\n");
        html.append("            <div class='thank-you'>\n");
        html.append("                <p><strong>Thank you for your business!</strong></p>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Get CSS styles for invoice
     */
    private String getInvoiceCSS() {
        return "body {" +
            "font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;" +
            "margin: 0;" +
            "padding: 20px;" +
            "background-color: #f8f9fa;" +
            "color: #34495e;" +
            "line-height: 1.4;" +
            "}" +
            ".invoice-container {" +
            "max-width: 800px;" +
            "margin: 0 auto;" +
            "background: white;" +
            "border-radius: 8px;" +
            "box-shadow: 0 0 20px rgba(0,0,0,0.1);" +
            "overflow: hidden;" +
            "}" +
            ".invoice-header {" +
            "background: linear-gradient(135deg, #34495e 0%, #2c3e50 100%);" +
            "color: white;" +
            "padding: 30px;" +
            "display: flex;" +
            "justify-content: space-between;" +
            "align-items: flex-start;" +
            "}" +
            ".company-info h1 {" +
            "margin: 0 0 15px 0;" +
            "font-size: 28px;" +
            "font-weight: 600;" +
            "}" +
            ".company-details p {" +
            "margin: 3px 0;" +
            "opacity: 0.9;" +
            "}" +
            ".invoice-title {" +
            "text-align: right;" +
            "}" +
            ".invoice-title h2 {" +
            "margin: 0 0 10px 0;" +
            "font-size: 36px;" +
            "font-weight: 300;" +
            "letter-spacing: 2px;" +
            "}" +
            ".invoice-number {" +
            "font-size: 18px;" +
            "font-weight: 600;" +
            "margin-bottom: 10px;" +
            "}" +
            ".invoice-status {" +
            "padding: 8px 16px;" +
            "border-radius: 20px;" +
            "font-size: 12px;" +
            "font-weight: 600;" +
            "text-transform: uppercase;" +
            "letter-spacing: 1px;" +
            "}" +
            ".status-draft { background: #95a5a6; color: white; }" +
            ".status-sent { background: #3498db; color: white; }" +
            ".status-viewed { background: #f39c12; color: white; }" +
            ".status-paid { background: #27ae60; color: white; }" +
            ".status-overdue { background: #e74c3c; color: white; }" +
            ".status-cancelled { background: #7f8c8d; color: white; }" +
            ".status-partially_paid { background: #e67e22; color: white; }" +
            ".invoice-details {" +
            "padding: 30px;" +
            "display: flex;" +
            "justify-content: space-between;" +
            "border-bottom: 2px solid #ecf0f1;" +
            "}" +
            ".customer-info h3, .invoice-meta h3 {" +
            "margin: 0 0 15px 0;" +
            "color: #34495e;" +
            "font-size: 16px;" +
            "font-weight: 600;" +
            "}" +
            ".company-name {" +
            "font-weight: 600;" +
            "font-size: 16px;" +
            "color: #2c3e50;" +
            "}" +
            ".customer-details p {" +
            "margin: 5px 0;" +
            "}" +
            ".invoice-meta table {" +
            "border-collapse: collapse;" +
            "}" +
            ".invoice-meta td {" +
            "padding: 5px 15px 5px 0;" +
            "font-size: 14px;" +
            "}" +
            ".invoice-meta td:first-child {" +
            "font-weight: 600;" +
            "color: #7f8c8d;" +
            "}" +
            ".invoice-items {" +
            "padding: 0 30px;" +
            "}" +
            ".items-table {" +
            "width: 100%;" +
            "border-collapse: collapse;" +
            "margin: 20px 0;" +
            "}" +
            ".items-table th {" +
            "background: #34495e;" +
            "color: white;" +
            "padding: 15px 10px;" +
            "text-align: left;" +
            "font-weight: 600;" +
            "font-size: 14px;" +
            "}" +
            ".items-table td {" +
            "padding: 12px 10px;" +
            "border-bottom: 1px solid #ecf0f1;" +
            "}" +
            ".items-table tbody tr:hover {" +
            "background-color: #f8f9fa;" +
            "}" +
            ".description {" +
            "max-width: 300px;" +
            "}" +
            ".item-description {" +
            "font-weight: 500;" +
            "margin-bottom: 5px;" +
            "}" +
            ".product-code {" +
            "font-size: 12px;" +
            "color: #7f8c8d;" +
            "font-style: italic;" +
            "}" +
            ".quantity, .unit-price, .discount, .total {" +
            "text-align: right;" +
            "font-weight: 500;" +
            "}" +
            ".unit {" +
            "text-align: center;" +
            "color: #7f8c8d;" +
            "}" +
            ".invoice-totals {" +
            "padding: 20px 30px;" +
            "background: #f8f9fa;" +
            "}" +
            ".totals-table {" +
            "width: 300px;" +
            "margin-left: auto;" +
            "border-collapse: collapse;" +
            "}" +
            ".totals-table td {" +
            "padding: 8px 15px;" +
            "text-align: right;" +
            "}" +
            ".totals-table td:first-child {" +
            "text-align: left;" +
            "color: #7f8c8d;" +
            "}" +
            ".total-row, .balance-row {" +
            "border-top: 2px solid #34495e;" +
            "font-size: 18px;" +
            "}" +
            ".total-row td, .balance-row td {" +
            "padding: 15px;" +
            "color: #2c3e50;" +
            "}" +
            ".discount {" +
            "color: #e74c3c;" +
            "}" +
            ".paid {" +
            "color: #27ae60;" +
            "}" +
            ".invoice-notes {" +
            "padding: 20px 30px;" +
            "border-top: 1px solid #ecf0f1;" +
            "}" +
            ".invoice-notes h3 {" +
            "margin: 0 0 10px 0;" +
            "color: #34495e;" +
            "}" +
            ".invoice-footer {" +
            "padding: 30px;" +
            "background: #34495e;" +
            "color: white;" +
            "text-align: center;" +
            "}" +
            ".payment-info {" +
            "margin-bottom: 20px;" +
            "}" +
            ".payment-info h3 {" +
            "margin: 0 0 15px 0;" +
            "font-size: 18px;" +
            "}" +
            ".payment-info p {" +
            "margin: 8px 0;" +
            "opacity: 0.9;" +
            "}" +
            ".thank-you p {" +
            "margin: 0;" +
            "font-size: 18px;" +
            "font-weight: 600;" +
            "}" +
            "@media print {" +
            "body { background: white; padding: 0; }" +
            ".invoice-container { box-shadow: none; border-radius: 0; }" +
            "}";
    }
    
    /**
     * Save invoice HTML to file
     */
    public void saveInvoiceToFile(Invoice invoice, String filePath) throws IOException {
        String html = generateHTMLInvoice(invoice);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(html);
        }
    }
    
    /**
     * Generate invoice filename
     */
    public String generateInvoiceFilename(Invoice invoice) {
        return "Invoice_" + invoice.getInvoiceNumber().replace("/", "_") + "_" + 
               invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".html";
    }
}
