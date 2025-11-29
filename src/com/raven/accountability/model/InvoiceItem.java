package com.raven.accountability.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Invoice Item Model for Business Accountability System
 * Represents individual line items in an invoice
 */
public class InvoiceItem {
    private Long itemId;
    private Invoice invoice;
    private String description;
    private String productCode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal taxRate;
    private BigDecimal total;
    private String unit; // e.g., "hours", "pieces", "kg", etc.
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    
    // Default constructor
    public InvoiceItem() {
        this.quantity = BigDecimal.ONE;
        this.unitPrice = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.taxRate = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.unit = "unit";
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    // Constructor with basic fields
    public InvoiceItem(String description, BigDecimal quantity, BigDecimal unitPrice) {
        this();
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateTotal();
    }
    
    // Constructor with all main fields
    public InvoiceItem(String description, String productCode, BigDecimal quantity, 
                      BigDecimal unitPrice, String unit) {
        this();
        this.description = description;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        calculateTotal();
    }
    
    // Getters and Setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { 
        this.productCode = productCode;
        this.lastModified = LocalDateTime.now();
    }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { 
        this.quantity = quantity;
        calculateTotal();
    }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { 
        this.unitPrice = unitPrice;
        calculateTotal();
    }
    
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { 
        this.discount = discount;
        calculateTotal();
    }
    
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { 
        this.taxRate = taxRate;
        calculateTotal();
    }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { 
        this.unit = unit;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    // Business methods
    public void calculateTotal() {
        if (quantity == null || unitPrice == null) {
            this.total = BigDecimal.ZERO;
            return;
        }
        
        // Calculate subtotal (quantity × unit price)
        BigDecimal subtotal = quantity.multiply(unitPrice);
        
        // Apply discount if any
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.subtract(discount);
        }
        
        // Apply tax if any
        if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal taxAmount = subtotal.multiply(taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            subtotal = subtotal.add(taxAmount);
        }
        
        this.total = subtotal.setScale(2, RoundingMode.HALF_UP);
        this.lastModified = LocalDateTime.now();
        
        // Trigger invoice recalculation if this item belongs to an invoice
        if (invoice != null) {
            invoice.recalculateAmounts();
        }
    }
    
    public BigDecimal getSubtotal() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getTaxAmount() {
        if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal subtotal = getSubtotal();
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.subtract(discount);
        }
        
        return subtotal.multiply(taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
               .setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getDiscountAmount() {
        return discount != null ? discount : BigDecimal.ZERO;
    }
    
    public boolean hasDiscount() {
        return discount != null && discount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean hasTax() {
        return taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public String getFormattedQuantity() {
        if (quantity == null) return "0";
        
        // Remove trailing zeros for display
        return quantity.stripTrailingZeros().toPlainString();
    }
    
    public String getFormattedUnitPrice() {
        if (unitPrice == null) return "$0.00";
        return "$" + unitPrice.setScale(2, RoundingMode.HALF_UP).toString();
    }
    
    public String getFormattedTotal() {
        if (total == null) return "$0.00";
        return "$" + total.setScale(2, RoundingMode.HALF_UP).toString();
    }
    
    @Override
    public String toString() {
        return description + " (" + getFormattedQuantity() + " " + unit + " × " + 
               getFormattedUnitPrice() + " = " + getFormattedTotal() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InvoiceItem that = (InvoiceItem) obj;
        return itemId != null && itemId.equals(that.itemId);
    }
    
    @Override
    public int hashCode() {
        return itemId != null ? itemId.hashCode() : 0;
    }
}
