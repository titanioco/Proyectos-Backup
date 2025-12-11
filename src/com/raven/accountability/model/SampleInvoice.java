package com.raven.accountability.model;

import java.math.BigDecimal;

/**
 * Sample Invoice class for demo mode
 */
public class SampleInvoice {
    private String invoiceNumber;
    private String customerName;
    private String date;
    private String dueDate;
    private String status;
    private String formattedAmount;
    private String formattedPaid;
    private String formattedBalance;
    private BigDecimal amount;
    private BigDecimal paid;
    private BigDecimal balance;
    
    public SampleInvoice(String invoiceNumber, String customerName, String date, 
                        String dueDate, String status, String formattedAmount,
                        String formattedPaid, String formattedBalance) {
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.date = date;
        this.dueDate = dueDate;
        this.status = status;
        this.formattedAmount = formattedAmount;
        this.formattedPaid = formattedPaid;
        this.formattedBalance = formattedBalance;
        
        // Parse amounts from formatted strings
        try {
            String cleanAmount = formattedAmount.replace("$", "").replace(",", "");
            this.amount = new BigDecimal(cleanAmount);
        } catch (NumberFormatException e) {
            this.amount = BigDecimal.ZERO;
        }
        
        try {
            String cleanPaid = formattedPaid.replace("$", "").replace(",", "");
            this.paid = new BigDecimal(cleanPaid);
        } catch (NumberFormatException e) {
            this.paid = BigDecimal.ZERO;
        }
        
        try {
            String cleanBalance = formattedBalance.replace("$", "").replace(",", "");
            this.balance = new BigDecimal(cleanBalance);
        } catch (NumberFormatException e) {
            this.balance = BigDecimal.ZERO;
        }
    }
    
    // Getters and setters
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getFormattedAmount() { return formattedAmount; }
    public void setFormattedAmount(String formattedAmount) { 
        this.formattedAmount = formattedAmount;
        try {
            String cleanAmount = formattedAmount.replace("$", "").replace(",", "");
            this.amount = new BigDecimal(cleanAmount);
        } catch (NumberFormatException e) {
            this.amount = BigDecimal.ZERO;
        }
    }
    
    public String getFormattedPaid() { return formattedPaid; }
    public void setFormattedPaid(String formattedPaid) { 
        this.formattedPaid = formattedPaid;
        try {
            String cleanPaid = formattedPaid.replace("$", "").replace(",", "");
            this.paid = new BigDecimal(cleanPaid);
        } catch (NumberFormatException e) {
            this.paid = BigDecimal.ZERO;
        }
    }
    
    public String getFormattedBalance() { return formattedBalance; }
    public void setFormattedBalance(String formattedBalance) { 
        this.formattedBalance = formattedBalance;
        try {
            String cleanBalance = formattedBalance.replace("$", "").replace(",", "");
            this.balance = new BigDecimal(cleanBalance);
        } catch (NumberFormatException e) {
            this.balance = BigDecimal.ZERO;
        }
    }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount;
        this.formattedAmount = "$" + amount.toString();
        // Recalculate balance when amount changes
        this.balance = this.amount.subtract(this.paid);
        this.formattedBalance = "$" + this.balance.toString();
    }
    
    public BigDecimal getPaid() { return paid; }
    public void setPaid(BigDecimal paid) { 
        this.paid = paid;
        this.formattedPaid = "$" + paid.toString();
        // Recalculate balance
        this.balance = this.amount.subtract(paid);
        this.formattedBalance = "$" + this.balance.toString();
    }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { 
        this.balance = balance;
        this.formattedBalance = "$" + balance.toString();
    }
    
    /**
     * Convert to table row data
     */
    public Object[] toTableRow() {
        return new Object[] {
            invoiceNumber,
            customerName,
            date,
            dueDate,
            status,
            formattedAmount,
            formattedPaid,
            formattedBalance
        };
    }
    
    @Override
    public String toString() {
        return "SampleInvoice{" +
                "invoiceNumber='" + invoiceNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", date='" + date + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", status='" + status + '\'' +
                ", amount=" + amount +
                ", paid=" + paid +
                ", balance=" + balance +
                '}';
    }
}