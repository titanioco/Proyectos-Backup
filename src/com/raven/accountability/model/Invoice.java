package com.raven.accountability.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice Model for Business Accountability System
 * Represents invoices in the billing system
 */
public class Invoice {
    private Long invoiceId;
    private String invoiceNumber;
    private Customer customer;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private String description;
    private List<InvoiceItem> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private String createdBy;
    private String lastModifiedBy;
    
    // Payment tracking
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private LocalDate lastPaymentDate;
    
    public enum InvoiceStatus {
        DRAFT("Draft", "#95a5a6"),
        SENT("Sent", "#3498db"),
        VIEWED("Viewed", "#f39c12"),
        PAID("Paid", "#27ae60"),
        OVERDUE("Overdue", "#e74c3c"),
        CANCELLED("Cancelled", "#7f8c8d"),
        PARTIALLY_PAID("Partially Paid", "#e67e22");
        
        private final String displayName;
        private final String color;
        
        InvoiceStatus(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    // Default constructor
    public Invoice() {
        this.items = new ArrayList<>();
        this.status = InvoiceStatus.DRAFT;
        this.currency = "USD";
        this.subtotal = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.paidAmount = BigDecimal.ZERO;
        this.balanceAmount = BigDecimal.ZERO;
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.invoiceDate = LocalDate.now();
        generateInvoiceNumber();
    }
    
    // Constructor with customer
    public Invoice(Customer customer) {
        this();
        this.customer = customer;
        if (customer != null && customer.getPaymentTerms() != null) {
            this.dueDate = this.invoiceDate.plusDays(customer.getPaymentTerms().getDays());
        } else {
            this.dueDate = this.invoiceDate.plusDays(30); // Default 30 days
        }
    }
    
    private void generateInvoiceNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String sequence = String.format("%06d", System.currentTimeMillis() % 1000000);
        this.invoiceNumber = "INV-" + year + "-" + sequence;
    }
    
    // Getters and Setters
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { 
        this.invoiceNumber = invoiceNumber;
        this.lastModified = LocalDateTime.now();
    }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { 
        this.customer = customer;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { 
        this.invoiceDate = invoiceDate;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { 
        this.dueDate = dueDate;
        this.lastModified = LocalDateTime.now();
    }
    
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { 
        this.status = status;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.lastModified = LocalDateTime.now();
    }
    
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { 
        this.items = items;
        recalculateAmounts();
    }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { 
        this.discountAmount = discountAmount;
        recalculateAmounts();
    }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { 
        this.currency = currency;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { 
        this.notes = notes;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { 
        this.paidAmount = paidAmount;
        updateBalanceAndStatus();
    }
    
    public BigDecimal getBalanceAmount() { return balanceAmount; }
    public void setBalanceAmount(BigDecimal balanceAmount) { this.balanceAmount = balanceAmount; }
    
    public LocalDate getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(LocalDate lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
    
    // Business methods
    public void addItem(InvoiceItem item) {
        if (item != null) {
            this.items.add(item);
            item.setInvoice(this);
            recalculateAmounts();
        }
    }
    
    public void removeItem(InvoiceItem item) {
        if (item != null && this.items.remove(item)) {
            item.setInvoice(null);
            recalculateAmounts();
        }
    }
    
    public void recalculateAmounts() {
        // Calculate subtotal
        this.subtotal = items.stream()
            .map(InvoiceItem::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        
        // Apply discount
        BigDecimal discountedSubtotal = subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        
        // Calculate tax (this will be enhanced with tax management integration)
        // For now, assume 10% tax rate if taxAmount is not set
        if (taxAmount == null || taxAmount.equals(BigDecimal.ZERO)) {
            this.taxAmount = discountedSubtotal.multiply(new BigDecimal("0.10"))
                .setScale(2, RoundingMode.HALF_UP);
        }
        
        // Calculate total
        this.totalAmount = discountedSubtotal.add(taxAmount)
            .setScale(2, RoundingMode.HALF_UP);
        
        updateBalanceAndStatus();
        this.lastModified = LocalDateTime.now();
    }
    
    private void updateBalanceAndStatus() {
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        
        // Only update the balance amount, NEVER automatically change the status
        // This allows manual status control from the UI
        this.balanceAmount = totalAmount.subtract(paidAmount);
        
        // Status should only be set manually or during initial creation
        // No automatic status changes based on payment amounts
    }
    
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && balanceAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }
    
    public int getDaysOverdue() {
        if (isOverdue()) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }
        return 0;
    }
    
    public BigDecimal getPaymentProgress() {
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return paidAmount.divide(totalAmount, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String toString() {
        return invoiceNumber + " - " + 
               (customer != null ? customer.getCompanyName() : "No Customer") + 
               " (" + status.getDisplayName() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Invoice invoice = (Invoice) obj;
        return invoiceId != null && invoiceId.equals(invoice.invoiceId);
    }
    
    @Override
    public int hashCode() {
        return invoiceId != null ? invoiceId.hashCode() : 0;
    }
}
