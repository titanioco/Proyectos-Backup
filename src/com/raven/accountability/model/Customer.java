package com.raven.accountability.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer Model for Business Accountability System
 * Represents clients/customers in the billing system
 */
public class Customer {
    private Long customerId;
    private String customerCode;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String taxId;
    private CustomerStatus status;
    private PaymentTerms paymentTerms;
    private Double creditLimit;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private List<Invoice> invoices;
    
    public enum CustomerStatus {
        ACTIVE, INACTIVE, SUSPENDED, ARCHIVED
    }
    
    public enum PaymentTerms {
        NET_15(15), NET_30(30), NET_45(45), NET_60(60), NET_90(90), COD(0);
        
        private final int days;
        
        PaymentTerms(int days) {
            this.days = days;
        }
        
        public int getDays() {
            return days;
        }
    }
    
    // Default constructor
    public Customer() {
        this.invoices = new ArrayList<>();
        this.status = CustomerStatus.ACTIVE;
        this.paymentTerms = PaymentTerms.NET_30;
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public Customer(String companyName, String email, String contactPerson) {
        this();
        this.companyName = companyName;
        this.email = email;
        this.contactPerson = contactPerson;
        generateCustomerCode();
    }
    
    private void generateCustomerCode() {
        if (companyName != null && !companyName.trim().isEmpty()) {
            String code = companyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            if (code.length() > 6) {
                code = code.substring(0, 6);
            }
            this.customerCode = "CUST-" + code + "-" + System.currentTimeMillis() % 10000;
        }
    }
    
    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { 
        this.companyName = companyName;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { 
        this.contactPerson = contactPerson;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { 
        this.phone = phone;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { 
        this.address = address;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getCity() { return city; }
    public void setCity(String city) { 
        this.city = city;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getState() { return state; }
    public void setState(String state) { 
        this.state = state;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { 
        this.zipCode = zipCode;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { 
        this.country = country;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { 
        this.taxId = taxId;
        this.lastModified = LocalDateTime.now();
    }
    
    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { 
        this.status = status;
        this.lastModified = LocalDateTime.now();
    }
    
    public PaymentTerms getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(PaymentTerms paymentTerms) { 
        this.paymentTerms = paymentTerms;
        this.lastModified = LocalDateTime.now();
    }
    
    public Double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(Double creditLimit) { 
        this.creditLimit = creditLimit;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public List<Invoice> getInvoices() { return invoices; }
    public void setInvoices(List<Invoice> invoices) { this.invoices = invoices; }
    
    // Business methods
    public void addInvoice(Invoice invoice) {
        if (invoice != null) {
            this.invoices.add(invoice);
            invoice.setCustomer(this);
        }
    }
    
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        if (address != null && !address.trim().isEmpty()) {
            fullAddress.append(address);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city);
        }
        if (state != null && !state.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(state);
        }
        if (zipCode != null && !zipCode.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(" ");
            fullAddress.append(zipCode);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(country);
        }
        return fullAddress.toString();
    }
    
    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }
    
    @Override
    public String toString() {
        return companyName + " (" + customerCode + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer customer = (Customer) obj;
        return customerId != null && customerId.equals(customer.customerId);
    }
    
    @Override
    public int hashCode() {
        return customerId != null ? customerId.hashCode() : 0;
    }
}
