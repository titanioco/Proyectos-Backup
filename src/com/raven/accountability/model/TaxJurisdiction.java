package com.raven.accountability.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tax Jurisdiction Model for Business Accountability System
 * Represents different tax jurisdictions and their rates
 */
public class TaxJurisdiction {
    private Long jurisdictionId;
    private String jurisdictionCode;
    private String jurisdictionName;
    private String jurisdictionType; // FEDERAL, STATE, CITY, COUNTY
    private String country;
    private String state;
    private String city;
    private BigDecimal taxRate;
    private String taxType; // SALES_TAX, VAT, GST, INCOME_TAX, etc.
    private boolean isActive;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    
    // Default constructor
    public TaxJurisdiction() {
        this.isActive = true;
        this.effectiveDate = LocalDate.now();
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.taxRate = BigDecimal.ZERO;
    }
    
    // Constructor with required fields
    public TaxJurisdiction(String jurisdictionCode, String jurisdictionName, 
                          String jurisdictionType, BigDecimal taxRate) {
        this();
        this.jurisdictionCode = jurisdictionCode;
        this.jurisdictionName = jurisdictionName;
        this.jurisdictionType = jurisdictionType;
        this.taxRate = taxRate;
    }
    
    // Getters and Setters
    public Long getJurisdictionId() { return jurisdictionId; }
    public void setJurisdictionId(Long jurisdictionId) { this.jurisdictionId = jurisdictionId; }
    
    // Convenience methods for compatibility
    public Long getId() { return jurisdictionId; }
    public void setId(Long id) { this.jurisdictionId = id; }
    
    public String getJurisdictionCode() { return jurisdictionCode; }
    public void setJurisdictionCode(String jurisdictionCode) { 
        this.jurisdictionCode = jurisdictionCode;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getJurisdictionName() { return jurisdictionName; }
    public void setJurisdictionName(String jurisdictionName) { 
        this.jurisdictionName = jurisdictionName;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getJurisdictionType() { return jurisdictionType; }
    public void setJurisdictionType(String jurisdictionType) { 
        this.jurisdictionType = jurisdictionType;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { 
        this.country = country;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getState() { return state; }
    public void setState(String state) { 
        this.state = state;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getCity() { return city; }
    public void setCity(String city) { 
        this.city = city;
        this.lastModified = LocalDateTime.now();
    }
    
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { 
        this.taxRate = taxRate;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getTaxType() { return taxType; }
    public void setTaxType(String taxType) { 
        this.taxType = taxType;
        this.lastModified = LocalDateTime.now();
    }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { 
        isActive = active;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { 
        this.effectiveDate = effectiveDate;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { 
        this.expirationDate = expirationDate;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    // Business methods
    public boolean isValidForDate(LocalDate date) {
        if (!isActive) return false;
        if (effectiveDate != null && date.isBefore(effectiveDate)) return false;
        if (expirationDate != null && date.isAfter(expirationDate)) return false;
        return true;
    }
    
    public boolean isCurrentlyValid() {
        return isValidForDate(LocalDate.now());
    }
    
    public String getFormattedTaxRate() {
        if (taxRate == null) return "0.00%";
        return taxRate.toString() + "%";
    }
    
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(jurisdictionName);
        
        if (jurisdictionType != null) {
            fullName.append(" (").append(jurisdictionType).append(")");
        }
        
        return fullName.toString();
    }
    
    public String getLocationDescription() {
        StringBuilder location = new StringBuilder();
        
        if (city != null && !city.trim().isEmpty()) {
            location.append(city);
        }
        
        if (state != null && !state.trim().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(state);
        }
        
        if (country != null && !country.trim().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(country);
        }
        
        return location.toString();
    }
    
    @Override
    public String toString() {
        return getFullName() + " - " + getFormattedTaxRate();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TaxJurisdiction that = (TaxJurisdiction) obj;
        return jurisdictionId != null && jurisdictionId.equals(that.jurisdictionId);
    }
    
    @Override
    public int hashCode() {
        return jurisdictionId != null ? jurisdictionId.hashCode() : 0;
    }
}
