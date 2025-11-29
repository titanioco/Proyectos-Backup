package com.raven.accountability.service;

import com.raven.accountability.database.DatabaseManager;
import com.raven.accountability.model.Invoice;
import com.raven.accountability.model.InvoiceItem;
import com.raven.accountability.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Invoice Service for Business Accountability System
 * Handles all invoice-related database operations
 */
public class InvoiceService {
    private DatabaseManager dbManager;
    private CustomerService customerService;
    
    public InvoiceService() {
        try {
            this.dbManager = DatabaseManager.getInstance();
            // Don't test the connection here - let individual methods handle connection failures
            System.out.println("SUCCESS: InvoiceService database manager initialized");
            this.customerService = new CustomerService();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize InvoiceService: " + e.getMessage());
            // Continue with null dbManager - methods will need to handle this gracefully
            this.dbManager = null;
            this.customerService = null;
        }
    }
    
    /**
     * Save a new invoice to the database
     */
    public Invoice saveInvoice(Invoice invoice) throws SQLException {
        if (dbManager == null) {
            throw new SQLException("Database connection not available - running in limited mode");
        }
        
        // Check if invoice already exists
        Invoice existingInvoice = null;
        try {
            existingInvoice = findInvoiceByNumber(invoice.getInvoiceNumber());
        } catch (SQLException e) {
            // Invoice doesn't exist, continue with insert
        }
        
        if (existingInvoice != null) {
            // Invoice exists, update it instead
            System.out.println("InvoiceService: Invoice " + invoice.getInvoiceNumber() + " already exists, updating instead of inserting");
            
            // Copy the database ID to the new invoice object
            invoice.setInvoiceId(existingInvoice.getInvoiceId());
            
            // Use updateInvoiceBasicFields for safety
            return updateInvoiceBasicFields(invoice);
        }
        
        // Validate invoice for new insert
        if (invoice == null) {
            throw new SQLException("Invoice cannot be null");
        }
        if (invoice.getCustomer() == null) {
            throw new SQLException("Invoice customer cannot be null");
        }
        if (invoice.getCustomer().getCustomerId() == null) {
            throw new SQLException("Invoice customer ID cannot be null");
        }
        if (invoice.getInvoiceNumber() == null) {
            throw new SQLException("Invoice number cannot be null");
        }
        
        Connection conn = dbManager.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Insert invoice
            String invoiceSQL = "INSERT INTO invoices (invoice_number, customer_id, invoice_date, due_date, " +
                              "status, description, subtotal, tax_amount, discount_amount, total_amount, " +
                              "paid_amount, balance_amount, currency, notes, created_by) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(invoiceSQL, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, invoice.getInvoiceNumber());
            pstmt.setLong(2, invoice.getCustomer().getCustomerId());
            pstmt.setDate(3, Date.valueOf(invoice.getInvoiceDate()));
            pstmt.setDate(4, Date.valueOf(invoice.getDueDate()));
            pstmt.setString(5, invoice.getStatus().name());
            pstmt.setString(6, invoice.getDescription());
            pstmt.setBigDecimal(7, invoice.getSubtotal());
            pstmt.setBigDecimal(8, invoice.getTaxAmount());
            pstmt.setBigDecimal(9, invoice.getDiscountAmount());
            pstmt.setBigDecimal(10, invoice.getTotalAmount());
            pstmt.setBigDecimal(11, invoice.getPaidAmount());
            pstmt.setBigDecimal(12, invoice.getBalanceAmount());
            pstmt.setString(13, invoice.getCurrency());
            pstmt.setString(14, invoice.getNotes());
            pstmt.setString(15, invoice.getCreatedBy());
            
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            
            if (rs.next()) {
                long invoiceId = rs.getLong(1);
                invoice.setInvoiceId(invoiceId);
                
                // Insert invoice items
                for (InvoiceItem item : invoice.getItems()) {
                    saveInvoiceItem(item, invoiceId, conn);
                }
            }
            
            rs.close();
            pstmt.close();
            conn.commit();
            
            return invoice;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Update an existing invoice
     */
    public Invoice updateInvoice(Invoice invoice) throws SQLException {
        if (dbManager == null) {
            throw new SQLException("Database connection not available - running in limited mode");
        }
        
        Connection conn = dbManager.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Update invoice
            String invoiceSQL = "UPDATE invoices SET customer_id = ?, invoice_date = ?, due_date = ?, " +
                              "status = ?, description = ?, subtotal = ?, tax_amount = ?, discount_amount = ?, " +
                              "total_amount = ?, paid_amount = ?, balance_amount = ?, currency = ?, notes = ?, " +
                              "last_modified = CURRENT_TIMESTAMP, last_modified_by = ? " +
                              "WHERE invoice_id = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(invoiceSQL);
            pstmt.setLong(1, invoice.getCustomer().getCustomerId());
            pstmt.setDate(2, Date.valueOf(invoice.getInvoiceDate()));
            pstmt.setDate(3, Date.valueOf(invoice.getDueDate()));
            pstmt.setString(4, invoice.getStatus().name());
            pstmt.setString(5, invoice.getDescription());
            pstmt.setBigDecimal(6, invoice.getSubtotal());
            pstmt.setBigDecimal(7, invoice.getTaxAmount());
            pstmt.setBigDecimal(8, invoice.getDiscountAmount());
            pstmt.setBigDecimal(9, invoice.getTotalAmount());
            pstmt.setBigDecimal(10, invoice.getPaidAmount());
            pstmt.setBigDecimal(11, invoice.getBalanceAmount());
            pstmt.setString(12, invoice.getCurrency());
            pstmt.setString(13, invoice.getNotes());
            pstmt.setString(14, invoice.getLastModifiedBy());
            pstmt.setLong(15, invoice.getInvoiceId());
            
            int rowsUpdated = pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("InvoiceService: Updated " + rowsUpdated + " rows for invoice " + invoice.getInvoiceNumber());
            
            // Delete existing items and insert new ones
            String deleteItemsSQL = "DELETE FROM invoice_items WHERE invoice_id = ?";
            pstmt = conn.prepareStatement(deleteItemsSQL);
            pstmt.setLong(1, invoice.getInvoiceId());
            pstmt.executeUpdate();
            pstmt.close();
            
            // Insert updated items
            for (InvoiceItem item : invoice.getItems()) {
                saveInvoiceItem(item, invoice.getInvoiceId(), conn);
            }
            
            conn.commit();
            System.out.println("InvoiceService: Successfully committed update for invoice " + invoice.getInvoiceNumber());
            return invoice;
            
        } catch (SQLException e) {
            System.err.println("InvoiceService: Error updating invoice " + invoice.getInvoiceNumber() + ": " + e.getMessage());
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Update only basic invoice fields (simpler version without touching items)
     */
    public Invoice updateInvoiceBasicFields(Invoice invoice) throws SQLException {
        if (dbManager == null) {
            throw new SQLException("Database connection not available - running in limited mode");
        }
        
        // Validate invoice and its required fields before attempting update
        if (invoice == null) {
            throw new SQLException("Invoice cannot be null");
        }
        
        if (invoice.getInvoiceId() == null) {
            throw new SQLException("Invoice ID cannot be null for update - Invoice: " + invoice.getInvoiceNumber());
        }
        
        if (invoice.getCustomer() == null || invoice.getCustomer().getCustomerId() == null) {
            throw new SQLException("Customer or Customer ID cannot be null - Invoice: " + invoice.getInvoiceNumber());
        }
        
        System.out.println("InvoiceService: Starting basic field update for invoice " + invoice.getInvoiceNumber() + 
                         " (ID: " + invoice.getInvoiceId() + ")");
        System.out.println("InvoiceService: Update parameters - Customer ID: " + invoice.getCustomer().getCustomerId() + 
                         ", Status: " + invoice.getStatus() + 
                         ", Total: " + invoice.getTotalAmount() + 
                         ", Paid: " + invoice.getPaidAmount());
        
        // Use explicit transaction to ensure atomicity and immediate visibility
        Connection conn = dbManager.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        
        try {
            // Start explicit transaction
            conn.setAutoCommit(false);
            
            String sql = "UPDATE invoices SET customer_id = ?, invoice_date = ?, due_date = ?, " +
                        "status = ?, total_amount = ?, paid_amount = ?, balance_amount = ?, " +
                        "last_modified = CURRENT_TIMESTAMP, last_modified_by = ? " +
                        "WHERE invoice_id = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, invoice.getCustomer().getCustomerId());
            pstmt.setDate(2, Date.valueOf(invoice.getInvoiceDate()));
            pstmt.setDate(3, Date.valueOf(invoice.getDueDate()));
            pstmt.setString(4, invoice.getStatus().name());
            pstmt.setBigDecimal(5, invoice.getTotalAmount());
            pstmt.setBigDecimal(6, invoice.getPaidAmount());
            pstmt.setBigDecimal(7, invoice.getBalanceAmount());
            pstmt.setString(8, invoice.getLastModifiedBy());
            pstmt.setLong(9, invoice.getInvoiceId());
            
            int rowsUpdated = pstmt.executeUpdate();
            pstmt.close();
            
            if (rowsUpdated > 0) {
                // Commit the transaction immediately
                conn.commit();
                
                System.out.println("InvoiceService: Updated " + rowsUpdated + " rows for invoice " + 
                                 invoice.getInvoiceNumber() + " with basic fields - Status: " + invoice.getStatus() + 
                                 ", Amount: " + invoice.getTotalAmount() + ", Paid: " + invoice.getPaidAmount());
                
                // Force WAL checkpoint to ensure changes are visible to all connections
                try {
                    PreparedStatement checkpointStmt = conn.prepareStatement("PRAGMA wal_checkpoint");
                    checkpointStmt.execute();
                    checkpointStmt.close();
                    System.out.println("InvoiceService: WAL checkpoint executed successfully");
                } catch (SQLException e) {
                    System.err.println("InvoiceService: Warning - Could not execute WAL checkpoint: " + e.getMessage());
                }
                
                // Verify the update by fetching the invoice again from the same connection
                try {
                    String verifySQL = "SELECT i.*, c.company_name FROM invoices i " +
                                     "JOIN customers c ON i.customer_id = c.customer_id " +
                                     "WHERE i.invoice_id = ?";
                    PreparedStatement verifyStmt = conn.prepareStatement(verifySQL);
                    verifyStmt.setLong(1, invoice.getInvoiceId());
                    ResultSet rs = verifyStmt.executeQuery();
                    
                    if (rs.next()) {
                        Invoice updatedInvoice = mapResultSetToInvoice(rs);
                        System.out.println("InvoiceService: Verification successful - Updated invoice status: " + 
                                         updatedInvoice.getStatus() + 
                                         ", Amount: " + updatedInvoice.getTotalAmount() + 
                                         ", Paid: " + updatedInvoice.getPaidAmount());
                        rs.close();
                        verifyStmt.close();
                        return updatedInvoice; // Return the fresh copy from database
                    } else {
                        rs.close();
                        verifyStmt.close();
                        System.err.println("InvoiceService: Warning - Could not verify update for invoice " + invoice.getInvoiceNumber());
                        return invoice; // Return original invoice if verification fails
                    }
                } catch (Exception verifyEx) {
                    System.err.println("InvoiceService: Could not verify update: " + verifyEx.getMessage());
                    return invoice; // Return original invoice if verification fails
                }
            } else {
                conn.rollback();
                throw new SQLException("No rows were updated for invoice " + invoice.getInvoiceNumber() + 
                                     " (ID: " + invoice.getInvoiceId() + ") - Invoice may not exist in database");
            }
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("InvoiceService: Error during rollback: " + rollbackEx.getMessage());
            }
            
            System.err.println("InvoiceService: SQL Error in basic field update for invoice " + 
                             invoice.getInvoiceNumber() + " (ID: " + invoice.getInvoiceId() + "): " + e.getMessage());
            
            // Check if this is a constraint violation
            if (e.getMessage().contains("UNIQUE constraint") || e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                System.err.println("InvoiceService: UNIQUE constraint violation detected during UPDATE operation");
                System.err.println("InvoiceService: This should not happen for UPDATE with WHERE invoice_id = ?");
                System.err.println("InvoiceService: Possible causes:");
                System.err.println("InvoiceService: 1. Invoice ID " + invoice.getInvoiceId() + " does not exist");
                System.err.println("InvoiceService: 2. Database transaction issue");
                System.err.println("InvoiceService: 3. Concurrent modification");
                
                // Try to diagnose the issue
                try {
                    Invoice existingInvoice = findInvoiceByNumber(invoice.getInvoiceNumber());
                    if (existingInvoice != null) {
                        System.err.println("InvoiceService: Diagnosis - Found invoice with number " + 
                                         invoice.getInvoiceNumber() + " and ID " + existingInvoice.getInvoiceId());
                        if (!existingInvoice.getInvoiceId().equals(invoice.getInvoiceId())) {
                            System.err.println("InvoiceService: ERROR - ID MISMATCH! Trying to update ID " + 
                                             invoice.getInvoiceId() + " but database has ID " + existingInvoice.getInvoiceId());
                        }
                    } else {
                        System.err.println("InvoiceService: Diagnosis - No invoice found with number " + invoice.getInvoiceNumber());
                    }
                } catch (Exception diagEx) {
                    System.err.println("InvoiceService: Could not diagnose issue: " + diagEx.getMessage());
                }
            }
            
            throw e;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("InvoiceService: Error during rollback: " + rollbackEx.getMessage());
            }
            
            System.err.println("InvoiceService: Unexpected error in basic field update for invoice " + 
                             invoice.getInvoiceNumber() + ": " + e.getMessage());
            throw new SQLException("Unexpected error during invoice update: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                System.err.println("InvoiceService: Error restoring auto-commit: " + e.getMessage());
            }
        }
    }
    
    /**
     * Save invoice item
     */
    private void saveInvoiceItem(InvoiceItem item, Long invoiceId, Connection conn) throws SQLException {
        String itemSQL = "INSERT INTO invoice_items (invoice_id, description, product_code, quantity, " +
                        "unit_price, discount, tax_rate, total, unit) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = conn.prepareStatement(itemSQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setLong(1, invoiceId);
        pstmt.setString(2, item.getDescription());
        pstmt.setString(3, item.getProductCode());
        pstmt.setBigDecimal(4, item.getQuantity());
        pstmt.setBigDecimal(5, item.getUnitPrice());
        pstmt.setBigDecimal(6, item.getDiscount());
        pstmt.setBigDecimal(7, item.getTaxRate());
        pstmt.setBigDecimal(8, item.getTotal());
        pstmt.setString(9, item.getUnit());
        
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();
        
        if (rs.next()) {
            item.setItemId(rs.getLong(1));
        }
        
        rs.close();
        pstmt.close();
    }
    
    /**
     * Find invoice by ID
     */
    public Invoice findInvoiceById(Long invoiceId) throws SQLException {
        String sql = "SELECT i.*, c.company_name FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.invoice_id = ?";
        
        ResultSet rs = dbManager.executeQuery(sql, invoiceId);
        
        if (rs.next()) {
            Invoice invoice = mapResultSetToInvoice(rs);
            rs.close();
            
            // Load invoice items
            loadInvoiceItems(invoice);
            
            return invoice;
        }
        
        rs.close();
        return null;
    }
    
    /**
     * Find invoice by invoice number
     */
    public Invoice findInvoiceByNumber(String invoiceNumber) throws SQLException {
        String sql = "SELECT i.*, c.company_name FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.invoice_number = ?";
        
        ResultSet rs = dbManager.executeQuery(sql, invoiceNumber);
        
        if (rs.next()) {
            Invoice invoice = mapResultSetToInvoice(rs);
            rs.close();
            
            // Load invoice items
            loadInvoiceItems(invoice);
            
            return invoice;
        }
        
        rs.close();
        return null;
    }
    
    /**
     * Get all invoices
     */
    public List<Invoice> getAllInvoices() throws SQLException {
        if (dbManager == null) {
            System.out.println("WARNING: Database not available, returning empty invoice list");
            return new ArrayList<>();
        }
        
        String sql = "SELECT i.*, c.company_name FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "ORDER BY i.invoice_date DESC";
        
        ResultSet rs = dbManager.executeQuery(sql);
        
        List<Invoice> invoices = new ArrayList<>();
        while (rs.next()) {
            Invoice invoice = mapResultSetToInvoice(rs);
            loadInvoiceItems(invoice);
            invoices.add(invoice);
        }
        
        rs.close();
        return invoices;
    }
    
    /**
     * Get invoices by customer
     */
    public List<Invoice> getInvoicesByCustomer(Long customerId) throws SQLException {
        String sql = "SELECT i.*, c.company_name FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.customer_id = ? ORDER BY i.invoice_date DESC";
        
        ResultSet rs = dbManager.executeQuery(sql, customerId);
        
        List<Invoice> invoices = new ArrayList<>();
        while (rs.next()) {
            Invoice invoice = mapResultSetToInvoice(rs);
            loadInvoiceItems(invoice);
            invoices.add(invoice);
        }
        
        rs.close();
        return invoices;
    }
    
    /**
     * Get overdue invoices
     */
    public List<Invoice> getOverdueInvoices() throws SQLException {
        String sql = "SELECT i.*, c.company_name FROM invoices i " +
                    "JOIN customers c ON i.customer_id = c.customer_id " +
                    "WHERE i.due_date < DATE('now') AND i.balance_amount > 0 " +
                    "ORDER BY i.due_date ASC";
        
        ResultSet rs = dbManager.executeQuery(sql);
        
        List<Invoice> invoices = new ArrayList<>();
        while (rs.next()) {
            Invoice invoice = mapResultSetToInvoice(rs);
            loadInvoiceItems(invoice);
            invoices.add(invoice);
        }
        
        rs.close();
        return invoices;
    }
    
    /**
     * Update invoice payment
     */
    public void updateInvoicePayment(Long invoiceId, BigDecimal paidAmount, String paymentDate) throws SQLException {
        String sql = "UPDATE invoices SET paid_amount = ?, balance_amount = total_amount - ?, " +
                    "last_payment_date = ?, last_modified = CURRENT_TIMESTAMP " +
                    "WHERE invoice_id = ?";
        
        dbManager.executeUpdate(sql, paidAmount, paidAmount, paymentDate, invoiceId);
        
        // Update status based on payment
        updateInvoiceStatus(invoiceId);
    }
    
    /**
     * Update invoice status based on payment
     */
    private void updateInvoiceStatus(Long invoiceId) throws SQLException {
        String sql = "UPDATE invoices SET status = " +
                    "CASE " +
                    "  WHEN balance_amount <= 0 THEN 'PAID' " +
                    "  WHEN paid_amount > 0 THEN 'PARTIALLY_PAID' " +
                    "  WHEN due_date < DATE('now') THEN 'OVERDUE' " +
                    "  ELSE status " +
                    "END " +
                    "WHERE invoice_id = ?";
        
        dbManager.executeUpdate(sql, invoiceId);
    }
    
    /**
     * Delete invoice
     */
    public boolean deleteInvoice(Long invoiceId) throws SQLException {
        String sql = "DELETE FROM invoices WHERE invoice_id = ?";
        int rowsAffected = dbManager.executeUpdate(sql, invoiceId);
        return rowsAffected > 0;
    }
    
    /**
     * Get invoice statistics
     */
    public InvoiceStats getInvoiceStats() throws SQLException {
        InvoiceStats stats = new InvoiceStats();
        
        // Total invoices
        String totalSQL = "SELECT COUNT(*), SUM(total_amount), SUM(paid_amount), SUM(balance_amount) FROM invoices";
        ResultSet rs = dbManager.executeQuery(totalSQL);
        if (rs.next()) {
            stats.totalInvoices = rs.getInt(1);
            stats.totalAmount = rs.getBigDecimal(2) != null ? rs.getBigDecimal(2) : BigDecimal.ZERO;
            stats.totalPaid = rs.getBigDecimal(3) != null ? rs.getBigDecimal(3) : BigDecimal.ZERO;
            stats.totalOutstanding = rs.getBigDecimal(4) != null ? rs.getBigDecimal(4) : BigDecimal.ZERO;
        }
        rs.close();
        
        // Overdue invoices
        String overdueSQL = "SELECT COUNT(*), SUM(balance_amount) FROM invoices " +
                           "WHERE due_date < DATE('now') AND balance_amount > 0";
        rs = dbManager.executeQuery(overdueSQL);
        if (rs.next()) {
            stats.overdueInvoices = rs.getInt(1);
            stats.overdueAmount = rs.getBigDecimal(2) != null ? rs.getBigDecimal(2) : BigDecimal.ZERO;
        }
        rs.close();
        
        return stats;
    }
    
    /**
     * Load invoice items for an invoice
     */
    private void loadInvoiceItems(Invoice invoice) throws SQLException {
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ? ORDER BY item_id";
        ResultSet rs = dbManager.executeQuery(sql, invoice.getInvoiceId());
        
        List<InvoiceItem> items = new ArrayList<>();
        while (rs.next()) {
            InvoiceItem item = mapResultSetToInvoiceItem(rs);
            item.setInvoice(invoice);
            items.add(item);
        }
        
        invoice.setItems(items);
        rs.close();
    }
    
    /**
     * Map ResultSet to Invoice object
     */
    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(rs.getLong("invoice_id"));
        invoice.setInvoiceNumber(rs.getString("invoice_number"));
        invoice.setInvoiceDate(rs.getDate("invoice_date").toLocalDate());
        invoice.setDueDate(rs.getDate("due_date").toLocalDate());
        invoice.setStatus(Invoice.InvoiceStatus.valueOf(rs.getString("status")));
        invoice.setDescription(rs.getString("description"));
        invoice.setSubtotal(rs.getBigDecimal("subtotal"));
        invoice.setTaxAmount(rs.getBigDecimal("tax_amount"));
        invoice.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
        invoice.setPaidAmount(rs.getBigDecimal("paid_amount"));
        invoice.setBalanceAmount(rs.getBigDecimal("balance_amount"));
        invoice.setCurrency(rs.getString("currency"));
        invoice.setNotes(rs.getString("notes"));
        invoice.setCreatedBy(rs.getString("created_by"));
        invoice.setLastModifiedBy(rs.getString("last_modified_by"));
        
        // Handle timestamps
        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            invoice.setCreatedDate(createdDate.toLocalDateTime());
        }
        
        Timestamp lastModified = rs.getTimestamp("last_modified");
        if (lastModified != null) {
            invoice.setLastModified(lastModified.toLocalDateTime());
        }
        
        Date lastPaymentDate = rs.getDate("last_payment_date");
        if (lastPaymentDate != null) {
            invoice.setLastPaymentDate(lastPaymentDate.toLocalDate());
        }
        
        // Load customer
        try {
            Customer customer = customerService.findCustomerById(rs.getLong("customer_id"));
            invoice.setCustomer(customer);
        } catch (SQLException e) {
            System.err.println("Warning: Could not load customer for invoice " + invoice.getInvoiceNumber());
        }
        
        return invoice;
    }
    
    /**
     * Map ResultSet to InvoiceItem object
     */
    private InvoiceItem mapResultSetToInvoiceItem(ResultSet rs) throws SQLException {
        InvoiceItem item = new InvoiceItem();
        item.setItemId(rs.getLong("item_id"));
        item.setDescription(rs.getString("description"));
        item.setProductCode(rs.getString("product_code"));
        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setDiscount(rs.getBigDecimal("discount"));
        item.setTaxRate(rs.getBigDecimal("tax_rate"));
        item.setTotal(rs.getBigDecimal("total"));
        item.setUnit(rs.getString("unit"));
        
        // Handle timestamps
        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            item.setCreatedDate(createdDate.toLocalDateTime());
        }
        
        Timestamp lastModified = rs.getTimestamp("last_modified");
        if (lastModified != null) {
            item.setLastModified(lastModified.toLocalDateTime());
        }
        
        return item;
    }
    
    /**
     * Invoice statistics inner class
     */
    public static class InvoiceStats {
        public int totalInvoices;
        public BigDecimal totalAmount = BigDecimal.ZERO;
        public BigDecimal totalPaid = BigDecimal.ZERO;
        public BigDecimal totalOutstanding = BigDecimal.ZERO;
        public int overdueInvoices;
        public BigDecimal overdueAmount = BigDecimal.ZERO;
        
        @Override
        public String toString() {
            return "InvoiceStats{" +
                   "totalInvoices=" + totalInvoices +
                   ", totalAmount=" + totalAmount +
                   ", totalPaid=" + totalPaid +
                   ", totalOutstanding=" + totalOutstanding +
                   ", overdueInvoices=" + overdueInvoices +
                   ", overdueAmount=" + overdueAmount +
                   '}';
        }
    }
}
