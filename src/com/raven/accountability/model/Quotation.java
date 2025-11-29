package com.raven.accountability.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quotation Model for Business Accountability System
 * Represents quotations in the billing system
 */
public class Quotation {
    private Long quotationId;
    private String quotationNumber;
    private Customer customer;
    private LocalDate quotationDate;
    private LocalDate validUntilDate;
    private QuotationStatus status;
    private String description;
    private List<QuotationItem> items;
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
    
    // Conversion tracking
    private boolean convertedToInvoice;
    private String convertedInvoiceNumber;
    private LocalDate conversionDate;
    
    public enum QuotationStatus {
        DRAFT("Draft", "#95a5a6"),
        SENT("Sent", "#3498db"),
        VIEWED("Viewed", "#f39c12"),
        ACCEPTED("Accepted", "#27ae60"),
        REJECTED("Rejected", "#e74c3c"),
        EXPIRED("Expired", "#e67e22"),
        CONVERTED("Converted", "#8e44ad");
        
        private final String displayName;
        private final String color;
        
        QuotationStatus(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    public Quotation() {
        this.items = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.status = QuotationStatus.DRAFT;
        this.currency = "USD";
        this.quotationDate = LocalDate.now();
        this.validUntilDate = LocalDate.now().plusDays(30); // Default 30 days validity
        this.createdDate = LocalDateTime.now();
        this.convertedToInvoice = false;
    }
    
    // Getters and Setters
    public Long getQuotationId() {
        return quotationId;
    }
    
    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }
    
    public String getQuotationNumber() {
        return quotationNumber;
    }
    
    public void setQuotationNumber(String quotationNumber) {
        this.quotationNumber = quotationNumber;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public LocalDate getQuotationDate() {
        return quotationDate;
    }
    
    public void setQuotationDate(LocalDate quotationDate) {
        this.quotationDate = quotationDate;
    }
    
    public LocalDate getValidUntilDate() {
        return validUntilDate;
    }
    
    public void setValidUntilDate(LocalDate validUntilDate) {
        this.validUntilDate = validUntilDate;
    }
    
    public QuotationStatus getStatus() {
        return status;
    }
    
    public void setStatus(QuotationStatus status) {
        this.status = status;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<QuotationItem> getItems() {
        return items;
    }
    
    public void setItems(List<QuotationItem> items) {
        this.items = items;
        recalculateAmounts();
    }
    
    public void addItem(QuotationItem item) {
        this.items.add(item);
        recalculateAmounts();
    }
    
    public void removeItem(QuotationItem item) {
        this.items.remove(item);
        recalculateAmounts();
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }
    
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
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
    
    public LocalDate getConversionDate() {
        return conversionDate;
    }
    
    public void setConversionDate(LocalDate conversionDate) {
        this.conversionDate = conversionDate;
    }
    
    /**
     * Recalculate all amounts based on items
     */
    public void recalculateAmounts() {
        subtotal = items.stream()
                .map(QuotationItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate tax (assuming tax rate is stored somewhere or calculated)
        // For now, using a simple calculation
        BigDecimal taxRate = new BigDecimal("0.10"); // 10% tax
        taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        
        // Calculate total
        totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
        
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Check if quotation is expired
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(validUntilDate) && status != QuotationStatus.ACCEPTED 
               && status != QuotationStatus.CONVERTED && status != QuotationStatus.REJECTED;
    }
    
    /**
     * Convert this quotation to an invoice
     */
    public Invoice convertToInvoice() {
        if (convertedToInvoice) {
            throw new IllegalStateException("Quotation already converted to invoice");
        }
        
        Invoice invoice = new Invoice();
        invoice.setCustomer(this.customer);
        invoice.setDescription(this.description);
        invoice.setCurrency(this.currency);
        invoice.setNotes("Converted from Quotation: " + this.quotationNumber + 
                        (this.notes != null ? "\n" + this.notes : ""));
        invoice.setCreatedBy(this.lastModifiedBy);
        
        // Convert quotation items to invoice items
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        for (QuotationItem qItem : this.items) {
            InvoiceItem iItem = new InvoiceItem();
            iItem.setDescription(qItem.getDescription());
            iItem.setQuantity(qItem.getQuantity());
            iItem.setUnitPrice(qItem.getUnitPrice());
            iItem.setTotal(qItem.getTotal());
            invoiceItems.add(iItem);
        }
        invoice.setItems(invoiceItems);
        
        // Set amounts
        invoice.setSubtotal(this.subtotal);
        invoice.setTaxAmount(this.taxAmount);
        invoice.setDiscountAmount(this.discountAmount);
        invoice.setTotalAmount(this.totalAmount);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(this.totalAmount);
        
        // Mark this quotation as converted
        this.convertedToInvoice = true;
        this.conversionDate = LocalDate.now();
        this.status = QuotationStatus.CONVERTED;
        this.lastModified = LocalDateTime.now();
        
        return invoice;
    }
    
    @Override
    public String toString() {
        return quotationNumber + " - " + (customer != null ? customer.getCompanyName() : "No Customer") + 
               " - " + totalAmount + " " + currency;
    }
}
