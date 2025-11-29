package com.raven.accountability.ui.components;

import java.math.BigDecimal;

/**
 * Sample Invoice class for demonstration purposes  
 */
public class SampleInvoice {
    private String invoiceNumber;
    private String customerName;
    private String invoiceDate;
    private String dueDate;
    private String status;
    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal balance;
    private Long databaseId; // Store database ID for updates
    
    public SampleInvoice(String invoiceNumber, String customerName, String invoiceDate, 
                        String dueDate, String status, String amount, String paid, String balance) {
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.status = status;
        
        // Parse amounts, removing $ and commas
        try {
            this.amount = new BigDecimal(amount.replace("$", "").replace(",", ""));
        } catch (NumberFormatException e) {
            this.amount = BigDecimal.ZERO;
        }
        
        try {
            this.paid = new BigDecimal(paid.replace("$", "").replace(",", ""));
        } catch (NumberFormatException e) {
            this.paid = BigDecimal.ZERO;
        }
        
        try {
            this.balance = new BigDecimal(balance.replace("$", "").replace(",", ""));
        } catch (NumberFormatException e) {
            this.balance = BigDecimal.ZERO;
        }
    }
    
    // Getters and setters
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }
    
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount;
        // Recalculate balance when amount changes
        this.balance = amount.subtract(this.paid);
    }
    
    public BigDecimal getPaid() { return paid; }
    public void setPaid(BigDecimal paid) { 
        this.paid = paid;
        // Recalculate balance when paid changes
        this.balance = this.amount.subtract(paid);
    }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public Long getDatabaseId() { return databaseId; }
    public void setDatabaseId(Long databaseId) { this.databaseId = databaseId; }
    
    // Formatting methods
    public String getFormattedAmount() { return "$" + amount.toString(); }
    public String getFormattedPaid() { return "$" + paid.toString(); }
    public String getFormattedBalance() { return "$" + balance.toString(); }
    
    // Setter methods for formatted values
    public void setFormattedAmount(String formattedAmount) {
        try {
            this.amount = new BigDecimal(formattedAmount.replace("$", "").replace(",", ""));
            // Recalculate balance when amount changes
            this.balance = this.amount.subtract(this.paid);
        } catch (NumberFormatException e) {
            this.amount = BigDecimal.ZERO;
            this.balance = this.amount.subtract(this.paid);
        }
    }
    
    public void setFormattedPaid(String formattedPaid) {
        try {
            this.paid = new BigDecimal(formattedPaid.replace("$", "").replace(",", ""));
            // Recalculate balance when paid changes
            this.balance = this.amount.subtract(this.paid);
        } catch (NumberFormatException e) {
            this.paid = BigDecimal.ZERO;
            this.balance = this.amount.subtract(this.paid);
        }
    }
    
    // Date accessor method for compatibility
    public String getDate() {
        return invoiceDate;
    }
    
    // Convert to table row
    public Object[] toTableRow() {
        return new Object[]{
            invoiceNumber,
            customerName,
            invoiceDate,
            dueDate,
            status,
            getFormattedAmount(),
            getFormattedPaid(),
            getFormattedBalance()
        };
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SampleInvoice that = (SampleInvoice) obj;
        return invoiceNumber != null ? invoiceNumber.equals(that.invoiceNumber) : that.invoiceNumber == null;
    }
    
    @Override
    public int hashCode() {
        return invoiceNumber != null ? invoiceNumber.hashCode() : 0;
    }
}
