package com.raven.accountability.model;

import java.math.BigDecimal;

/**
 * Sample quotation model for demo purposes
 */
public class SampleQuotation {
    private String quotationNumber;
    private String customerName;
    private String quotationDate;
    private String validUntilDate;
    private String status;
    private String formattedAmount;
    private BigDecimal amount;
    private boolean convertedToInvoice;
    private String convertedInvoiceNumber;
    private String description;
    private String notes;
    
    public SampleQuotation(String quotationNumber, String customerName, String quotationDate, 
                          String validUntilDate, String status, String formattedAmount) {
        this.quotationNumber = quotationNumber;
        this.customerName = customerName;
        this.quotationDate = quotationDate;
        this.validUntilDate = validUntilDate;
        this.status = status;
        this.formattedAmount = formattedAmount;
        this.convertedToInvoice = false;
        this.description = "";
        this.notes = "";
        
        // Parse amount from formatted string
        try {
            String amountStr = formattedAmount.replace("$", "").replace(",", "");
            this.amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            this.amount = BigDecimal.ZERO;
        }
    }
    
    public SampleQuotation(String quotationNumber, String customerName, String quotationDate, 
                          String validUntilDate, String status, String formattedAmount,
                          String description, String notes) {
        this(quotationNumber, customerName, quotationDate, validUntilDate, status, formattedAmount);
        this.description = description != null ? description : "";
        this.notes = notes != null ? notes : "";
    }
    
    public SampleQuotation(String quotationNumber, String customerName, String quotationDate, 
                          String validUntilDate, String status, String formattedAmount,
                          boolean convertedToInvoice, String convertedInvoiceNumber) {
        this(quotationNumber, customerName, quotationDate, validUntilDate, status, formattedAmount);
        this.convertedToInvoice = convertedToInvoice;
        this.convertedInvoiceNumber = convertedInvoiceNumber;
    }
    
    // Getters and setters
    public String getQuotationNumber() {
        return quotationNumber;
    }
    
    public void setQuotationNumber(String quotationNumber) {
        this.quotationNumber = quotationNumber;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getQuotationDate() {
        return quotationDate;
    }
    
    public void setQuotationDate(String quotationDate) {
        this.quotationDate = quotationDate;
    }
    
    public String getValidUntilDate() {
        return validUntilDate;
    }
    
    public void setValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getFormattedAmount() {
        return formattedAmount;
    }
    
    public void setFormattedAmount(String formattedAmount) {
        this.formattedAmount = formattedAmount;
        // Update amount when formatted amount changes
        try {
            String amountStr = formattedAmount.replace("$", "").replace(",", "");
            this.amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            this.amount = BigDecimal.ZERO;
        }
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.formattedAmount = "$" + amount.toString();
    }
    
    public boolean isConvertedToInvoice() {
        return convertedToInvoice;
    }
    
    public void setConvertedToInvoice(boolean convertedToInvoice) {
        this.convertedToInvoice = convertedToInvoice;
    }
    
    public String getConvertedInvoiceNumber() {
        return convertedInvoiceNumber;
    }
    
    public void setConvertedInvoiceNumber(String convertedInvoiceNumber) {
        this.convertedInvoiceNumber = convertedInvoiceNumber;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    // Additional getter methods for compatibility
    public String getDate() {
        return quotationDate;
    }
    
    public String getValidUntil() {
        return validUntilDate;
    }
    
    /**
     * Convert to table row data
     */
    public Object[] toTableRow() {
        return new Object[]{
            quotationNumber,
            customerName,
            quotationDate,
            validUntilDate,
            status,
            formattedAmount,
            convertedToInvoice ? "Yes" : "No",
            convertedInvoiceNumber != null ? convertedInvoiceNumber : ""
        };
    }
    
    @Override
    public String toString() {
        return "SampleQuotation{" +
                "quotationNumber='" + quotationNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", status='" + status + '\'' +
                ", amount=" + amount +
                '}';
    }
}
