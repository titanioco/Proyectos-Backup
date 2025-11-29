package com.raven.accountability.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * QuotationItem Model for Business Accountability System
 * Represents individual line items in quotations
 */
public class QuotationItem {
    private Long itemId;
    private Long quotationId;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;
    private String notes;
    private int lineOrder;
    
    public QuotationItem() {
        this.quantity = BigDecimal.ONE;
        this.unitPrice = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.lineOrder = 0;
    }
    
    public QuotationItem(String description, BigDecimal quantity, BigDecimal unitPrice) {
        this();
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateTotal();
    }
    
    // Getters and Setters
    public Long getItemId() {
        return itemId;
    }
    
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    
    public Long getQuotationId() {
        return quotationId;
    }
    
    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateTotal();
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotal();
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public int getLineOrder() {
        return lineOrder;
    }
    
    public void setLineOrder(int lineOrder) {
        this.lineOrder = lineOrder;
    }
    
    /**
     * Calculate the total amount for this line item
     */
    public void calculateTotal() {
        if (quantity != null && unitPrice != null) {
            this.total = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        } else {
            this.total = BigDecimal.ZERO;
        }
    }
    
    /**
     * Create a formatted string representation of this item
     */
    public String getFormattedLine() {
        return String.format("%s - Qty: %s × $%s = $%s", 
                description != null ? description : "No description",
                quantity != null ? quantity.toString() : "0",
                unitPrice != null ? unitPrice.toString() : "0.00",
                total != null ? total.toString() : "0.00");
    }
    
    @Override
    public String toString() {
        return getFormattedLine();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        QuotationItem that = (QuotationItem) obj;
        
        if (itemId != null ? !itemId.equals(that.itemId) : that.itemId != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (quantity != null ? quantity.compareTo(that.quantity) != 0 : that.quantity != null) return false;
        return unitPrice != null ? unitPrice.compareTo(that.unitPrice) == 0 : that.unitPrice == null;
    }
    
    @Override
    public int hashCode() {
        int result = itemId != null ? itemId.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (unitPrice != null ? unitPrice.hashCode() : 0);
        return result;
    }
}
