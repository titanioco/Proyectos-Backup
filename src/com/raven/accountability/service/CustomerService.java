package com.raven.accountability.service;

import com.raven.accountability.database.DatabaseManager;
import com.raven.accountability.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer Service for Business Accountability System
 * Handles all customer-related database operations
 */
public class CustomerService {
    private DatabaseManager dbManager;
    
    public CustomerService() {
        try {
            this.dbManager = DatabaseManager.getInstance();
            System.out.println("SUCCESS: CustomerService initialized");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize CustomerService: " + e.getMessage());
            this.dbManager = null;
        }
    }
    
    /**
     * Save a new customer to the database
     */
    public Customer saveCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (customer_code, company_name, contact_person, email, phone, " +
                    "address, city, state, zip_code, country, tax_id, status, payment_terms, credit_limit) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        long generatedId = dbManager.executeInsert(sql,
            customer.getCustomerCode(),
            customer.getCompanyName(),
            customer.getContactPerson(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getAddress(),
            customer.getCity(),
            customer.getState(),
            customer.getZipCode(),
            customer.getCountry(),
            customer.getTaxId(),
            customer.getStatus().name(),
            customer.getPaymentTerms().name(),
            customer.getCreditLimit()
        );
        
        customer.setCustomerId(generatedId);
        return customer;
    }
    
    /**
     * Update an existing customer
     */
    public Customer updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET company_name = ?, contact_person = ?, email = ?, phone = ?, " +
                    "address = ?, city = ?, state = ?, zip_code = ?, country = ?, tax_id = ?, " +
                    "status = ?, payment_terms = ?, credit_limit = ?, last_modified = CURRENT_TIMESTAMP " +
                    "WHERE customer_id = ?";
        
        dbManager.executeUpdate(sql,
            customer.getCompanyName(),
            customer.getContactPerson(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getAddress(),
            customer.getCity(),
            customer.getState(),
            customer.getZipCode(),
            customer.getCountry(),
            customer.getTaxId(),
            customer.getStatus().name(),
            customer.getPaymentTerms().name(),
            customer.getCreditLimit(),
            customer.getCustomerId()
        );
        
        return customer;
    }
    
    /**
     * Find customer by ID
     */
    public Customer findCustomerById(Long customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        ResultSet rs = dbManager.executeQuery(sql, customerId);
        
        if (rs.next()) {
            return mapResultSetToCustomer(rs);
        }
        
        rs.close();
        return null;
    }
    
    /**
     * Find customer by customer code
     */
    public Customer findCustomerByCode(String customerCode) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_code = ?";
        ResultSet rs = dbManager.executeQuery(sql, customerCode);
        
        if (rs.next()) {
            Customer customer = mapResultSetToCustomer(rs);
            rs.close();
            return customer;
        }
        
        rs.close();
        return null;
    }
    
    /**
     * Find customers by company name (partial match)
     */
    public List<Customer> findCustomersByName(String companyName) throws SQLException {
        String sql = "SELECT * FROM customers WHERE LOWER(company_name) LIKE LOWER(?) ORDER BY company_name";
        ResultSet rs = dbManager.executeQuery(sql, "%" + companyName + "%");
        
        List<Customer> customers = new ArrayList<>();
        while (rs.next()) {
            customers.add(mapResultSetToCustomer(rs));
        }
        
        rs.close();
        return customers;
    }
    
    /**
     * Get all active customers
     */
    public List<Customer> getAllActiveCustomers() throws SQLException {
        String sql = "SELECT * FROM customers WHERE status = 'ACTIVE' ORDER BY company_name";
        ResultSet rs = dbManager.executeQuery(sql);
        
        List<Customer> customers = new ArrayList<>();
        while (rs.next()) {
            customers.add(mapResultSetToCustomer(rs));
        }
        
        rs.close();
        return customers;
    }
    
    /**
     * Get all customers
     */
    public List<Customer> getAllCustomers() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY company_name";
        ResultSet rs = dbManager.executeQuery(sql);
        
        List<Customer> customers = new ArrayList<>();
        while (rs.next()) {
            customers.add(mapResultSetToCustomer(rs));
        }
        
        rs.close();
        return customers;
    }
    
    /**
     * Delete customer by ID
     */
    public boolean deleteCustomer(Long customerId) throws SQLException {
        // First check if customer has invoices
        String checkInvoicesSQL = "SELECT COUNT(*) FROM invoices WHERE customer_id = ?";
        ResultSet rs = dbManager.executeQuery(checkInvoicesSQL, customerId);
        
        if (rs.next() && rs.getInt(1) > 0) {
            rs.close();
            throw new SQLException("Cannot delete customer with existing invoices. " +
                                 "Please delete or reassign invoices first.");
        }
        rs.close();
        
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        int rowsAffected = dbManager.executeUpdate(sql, customerId);
        return rowsAffected > 0;
    }
    
    /**
     * Check if customer code exists
     */
    public boolean customerCodeExists(String customerCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE customer_code = ?";
        ResultSet rs = dbManager.executeQuery(sql, customerCode);
        
        boolean exists = false;
        if (rs.next()) {
            exists = rs.getInt(1) > 0;
        }
        
        rs.close();
        return exists;
    }
    
    /**
     * Get customer statistics
     */
    public CustomerStats getCustomerStats() throws SQLException {
        CustomerStats stats = new CustomerStats();
        
        // Total customers
        String totalSQL = "SELECT COUNT(*) FROM customers";
        ResultSet rs = dbManager.executeQuery(totalSQL);
        if (rs.next()) {
            stats.totalCustomers = rs.getInt(1);
        }
        rs.close();
        
        // Active customers
        String activeSQL = "SELECT COUNT(*) FROM customers WHERE status = 'ACTIVE'";
        rs = dbManager.executeQuery(activeSQL);
        if (rs.next()) {
            stats.activeCustomers = rs.getInt(1);
        }
        rs.close();
        
        // Customers with invoices
        String withInvoicesSQL = "SELECT COUNT(DISTINCT customer_id) FROM invoices";
        rs = dbManager.executeQuery(withInvoicesSQL);
        if (rs.next()) {
            stats.customersWithInvoices = rs.getInt(1);
        }
        rs.close();
        
        return stats;
    }
    
    /**
     * Map ResultSet to Customer object
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getLong("customer_id"));
        customer.setCustomerCode(rs.getString("customer_code"));
        customer.setCompanyName(rs.getString("company_name"));
        customer.setContactPerson(rs.getString("contact_person"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        customer.setCity(rs.getString("city"));
        customer.setState(rs.getString("state"));
        customer.setZipCode(rs.getString("zip_code"));
        customer.setCountry(rs.getString("country"));
        customer.setTaxId(rs.getString("tax_id"));
        
        // Handle enums
        String status = rs.getString("status");
        if (status != null) {
            customer.setStatus(Customer.CustomerStatus.valueOf(status));
        }
        
        String paymentTerms = rs.getString("payment_terms");
        if (paymentTerms != null) {
            customer.setPaymentTerms(Customer.PaymentTerms.valueOf(paymentTerms));
        }
        
        java.math.BigDecimal creditLimit = rs.getBigDecimal("credit_limit");
        if (creditLimit != null) {
            customer.setCreditLimit(creditLimit.doubleValue());
        }
        
        // Handle timestamps
        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            customer.setCreatedDate(createdDate.toLocalDateTime());
        }
        
        Timestamp lastModified = rs.getTimestamp("last_modified");
        if (lastModified != null) {
            customer.setLastModified(lastModified.toLocalDateTime());
        }
        
        return customer;
    }
    
    /**
     * Customer statistics inner class
     */
    public static class CustomerStats {
        public int totalCustomers;
        public int activeCustomers;
        public int customersWithInvoices;
        
        @Override
        public String toString() {
            return "CustomerStats{" +
                   "totalCustomers=" + totalCustomers +
                   ", activeCustomers=" + activeCustomers +
                   ", customersWithInvoices=" + customersWithInvoices +
                   '}';
        }
    }
}
