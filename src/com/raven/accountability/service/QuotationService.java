package com.raven.accountability.service;

import com.raven.accountability.database.DatabaseManager;
import com.raven.accountability.model.Quotation;
import com.raven.accountability.model.QuotationItem;
import com.raven.accountability.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Quotation Service for Business Accountability System
 * Handles all quotation-related database operations
 */
public class QuotationService {
    private DatabaseManager dbManager;
    private CustomerService customerService;
    
    public QuotationService() {
        try {
            this.dbManager = DatabaseManager.getInstance();
            System.out.println("SUCCESS: QuotationService database manager initialized");
            this.customerService = new CustomerService();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize QuotationService: " + e.getMessage());
            this.dbManager = null;
            this.customerService = null;
        }
    }
    
    /**
     * Save a new quotation to the database
     */
    public Quotation saveQuotation(Quotation quotation) throws SQLException {
        if (dbManager == null) {
            throw new SQLException("Database connection not available - running in limited mode");
        }
        
        Connection conn = dbManager.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Insert quotation
            String quotationSQL = "INSERT INTO quotations (quotation_number, customer_id, quotation_date, valid_until_date, " +
                              "status, description, subtotal, tax_amount, discount_amount, total_amount, " +
                              "currency, notes, created_by, converted_to_invoice, converted_invoice_number) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement quotationStmt = conn.prepareStatement(quotationSQL, Statement.RETURN_GENERATED_KEYS);
            quotationStmt.setString(1, quotation.getQuotationNumber());
            quotationStmt.setLong(2, quotation.getCustomer().getCustomerId());
            quotationStmt.setDate(3, Date.valueOf(quotation.getQuotationDate()));
            quotationStmt.setDate(4, Date.valueOf(quotation.getValidUntilDate()));
            quotationStmt.setString(5, quotation.getStatus().name());
            quotationStmt.setString(6, quotation.getDescription());
            quotationStmt.setBigDecimal(7, quotation.getSubtotal());
            quotationStmt.setBigDecimal(8, quotation.getTaxAmount());
            quotationStmt.setBigDecimal(9, quotation.getDiscountAmount());
            quotationStmt.setBigDecimal(10, quotation.getTotalAmount());
            quotationStmt.setString(11, quotation.getCurrency());
            quotationStmt.setString(12, quotation.getNotes());
            quotationStmt.setString(13, quotation.getCreatedBy());
            quotationStmt.setBoolean(14, quotation.isConvertedToInvoice());
            quotationStmt.setString(15, quotation.getConvertedInvoiceNumber());
            
            quotationStmt.executeUpdate();
            
            // Get generated ID
            ResultSet generatedKeys = quotationStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                quotation.setQuotationId(generatedKeys.getLong(1));
            }
            
            // Insert quotation items
            if (quotation.getItems() != null && !quotation.getItems().isEmpty()) {
                String itemSQL = "INSERT INTO quotation_items (quotation_id, description, quantity, unit_price, total, notes, line_order) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement itemStmt = conn.prepareStatement(itemSQL);
                
                for (int i = 0; i < quotation.getItems().size(); i++) {
                    QuotationItem item = quotation.getItems().get(i);
                    itemStmt.setLong(1, quotation.getQuotationId());
                    itemStmt.setString(2, item.getDescription());
                    itemStmt.setBigDecimal(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getUnitPrice());
                    itemStmt.setBigDecimal(5, item.getTotal());
                    itemStmt.setString(6, item.getNotes());
                    itemStmt.setInt(7, i + 1);
                    itemStmt.addBatch();
                }
                
                itemStmt.executeBatch();
                itemStmt.close();
            }
            
            conn.commit();
            quotationStmt.close();
            
            System.out.println("SUCCESS: Quotation saved - " + quotation.getQuotationNumber());
            return quotation;
            
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("ERROR: Failed to save quotation - " + e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Update an existing quotation
     */
    public Quotation updateQuotation(Quotation quotation) throws SQLException {
        if (dbManager == null) {
            throw new SQLException("Database connection not available - running in limited mode");
        }
        
        Connection conn = dbManager.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Update quotation
            String quotationSQL = "UPDATE quotations SET customer_id = ?, quotation_date = ?, valid_until_date = ?, " +
                              "status = ?, description = ?, subtotal = ?, tax_amount = ?, discount_amount = ?, " +
                              "total_amount = ?, currency = ?, notes = ?, last_modified_by = ?, " +
                              "converted_to_invoice = ?, converted_invoice_number = ?, conversion_date = ?, " +
                              "last_modified = CURRENT_TIMESTAMP WHERE quotation_id = ?";
            
            PreparedStatement quotationStmt = conn.prepareStatement(quotationSQL);
            quotationStmt.setLong(1, quotation.getCustomer().getCustomerId());
            quotationStmt.setDate(2, Date.valueOf(quotation.getQuotationDate()));
            quotationStmt.setDate(3, Date.valueOf(quotation.getValidUntilDate()));
            quotationStmt.setString(4, quotation.getStatus().name());
            quotationStmt.setString(5, quotation.getDescription());
            quotationStmt.setBigDecimal(6, quotation.getSubtotal());
            quotationStmt.setBigDecimal(7, quotation.getTaxAmount());
            quotationStmt.setBigDecimal(8, quotation.getDiscountAmount());
            quotationStmt.setBigDecimal(9, quotation.getTotalAmount());
            quotationStmt.setString(10, quotation.getCurrency());
            quotationStmt.setString(11, quotation.getNotes());
            quotationStmt.setString(12, quotation.getLastModifiedBy());
            quotationStmt.setBoolean(13, quotation.isConvertedToInvoice());
            quotationStmt.setString(14, quotation.getConvertedInvoiceNumber());
            if (quotation.getConversionDate() != null) {
                quotationStmt.setDate(15, Date.valueOf(quotation.getConversionDate()));
            } else {
                quotationStmt.setNull(15, Types.DATE);
            }
            quotationStmt.setLong(16, quotation.getQuotationId());
            
            quotationStmt.executeUpdate();
            
            // Delete existing items
            String deleteItemsSQL = "DELETE FROM quotation_items WHERE quotation_id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteItemsSQL);
            deleteStmt.setLong(1, quotation.getQuotationId());
            deleteStmt.executeUpdate();
            deleteStmt.close();
            
            // Insert updated items
            if (quotation.getItems() != null && !quotation.getItems().isEmpty()) {
                String itemSQL = "INSERT INTO quotation_items (quotation_id, description, quantity, unit_price, total, notes, line_order) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement itemStmt = conn.prepareStatement(itemSQL);
                
                for (int i = 0; i < quotation.getItems().size(); i++) {
                    QuotationItem item = quotation.getItems().get(i);
                    itemStmt.setLong(1, quotation.getQuotationId());
                    itemStmt.setString(2, item.getDescription());
                    itemStmt.setBigDecimal(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getUnitPrice());
                    itemStmt.setBigDecimal(5, item.getTotal());
                    itemStmt.setString(6, item.getNotes());
                    itemStmt.setInt(7, i + 1);
                    itemStmt.addBatch();
                }
                
                itemStmt.executeBatch();
                itemStmt.close();
            }
            
            conn.commit();
            quotationStmt.close();
            
            System.out.println("SUCCESS: Quotation updated - " + quotation.getQuotationNumber());
            return quotation;
            
        } catch (SQLException e) {
            conn.rollback();
            System.err.println("ERROR: Failed to update quotation - " + e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Get all quotations
     */
    public List<Quotation> getAllQuotations() throws SQLException {
        if (dbManager == null) {
            throw new SQLException("Database connection not available - running in limited mode");
        }
        
        List<Quotation> quotations = new ArrayList<>();
        String sql = "SELECT q.*, c.company_name, c.contact_person, c.email, c.phone " +
                    "FROM quotations q " +
                    "LEFT JOIN customers c ON q.customer_id = c.customer_id " +
                    "ORDER BY q.quotation_date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Quotation quotation = mapResultSetToQuotation(rs);
                
                // Load items for this quotation
                List<QuotationItem> items = getQuotationItems(quotation.getQuotationId());
                quotation.setItems(items);
                
                quotations.add(quotation);
            }
        }
        
        return quotations;
    }
    
    /**
     * Find quotation by quotation number
     */
    public Quotation findQuotationByNumber(String quotationNumber) throws SQLException {
        if (dbManager == null) {
            throw new SQLException("Database connection not available - running in limited mode");
        }
        
        String sql = "SELECT q.*, c.company_name, c.contact_person, c.email, c.phone " +
                    "FROM quotations q " +
                    "LEFT JOIN customers c ON q.customer_id = c.customer_id " +
                    "WHERE q.quotation_number = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, quotationNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Quotation quotation = mapResultSetToQuotation(rs);
                    
                    // Load items for this quotation
                    List<QuotationItem> items = getQuotationItems(quotation.getQuotationId());
                    quotation.setItems(items);
                    
                    return quotation;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Delete a quotation
     */
    public boolean deleteQuotation(Long quotationId) throws SQLException {
        if (dbManager == null) {
            throw new SQLException("Database connection not available - running in limited mode");
        }
        
        Connection conn = dbManager.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Delete quotation items first
            String deleteItemsSQL = "DELETE FROM quotation_items WHERE quotation_id = ?";
            PreparedStatement deleteItemsStmt = conn.prepareStatement(deleteItemsSQL);
            deleteItemsStmt.setLong(1, quotationId);
            deleteItemsStmt.executeUpdate();
            deleteItemsStmt.close();
            
            // Delete quotation
            String deleteQuotationSQL = "DELETE FROM quotations WHERE quotation_id = ?";
            PreparedStatement deleteQuotationStmt = conn.prepareStatement(deleteQuotationSQL);
            deleteQuotationStmt.setLong(1, quotationId);
            int deleted = deleteQuotationStmt.executeUpdate();
            deleteQuotationStmt.close();
            
            conn.commit();
            return deleted > 0;
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Get quotation items for a specific quotation
     */
    private List<QuotationItem> getQuotationItems(Long quotationId) throws SQLException {
        List<QuotationItem> items = new ArrayList<>();
        String sql = "SELECT * FROM quotation_items WHERE quotation_id = ? ORDER BY line_order";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, quotationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuotationItem item = new QuotationItem();
                    item.setItemId(rs.getLong("item_id"));
                    item.setQuotationId(rs.getLong("quotation_id"));
                    item.setDescription(rs.getString("description"));
                    item.setQuantity(rs.getBigDecimal("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotal(rs.getBigDecimal("total"));
                    item.setNotes(rs.getString("notes"));
                    item.setLineOrder(rs.getInt("line_order"));
                    
                    items.add(item);
                }
            }
        }
        
        return items;
    }
    
    /**
     * Map ResultSet to Quotation object
     */
    private Quotation mapResultSetToQuotation(ResultSet rs) throws SQLException {
        Quotation quotation = new Quotation();
        quotation.setQuotationId(rs.getLong("quotation_id"));
        quotation.setQuotationNumber(rs.getString("quotation_number"));
        
        // Map customer if present
        if (rs.getString("company_name") != null) {
            Customer customer = new Customer();
            customer.setCustomerId(rs.getLong("customer_id"));
            customer.setCompanyName(rs.getString("company_name"));
            customer.setContactPerson(rs.getString("contact_person"));
            customer.setEmail(rs.getString("email"));
            customer.setPhone(rs.getString("phone"));
            quotation.setCustomer(customer);
        }
        
        quotation.setQuotationDate(rs.getDate("quotation_date").toLocalDate());
        quotation.setValidUntilDate(rs.getDate("valid_until_date").toLocalDate());
        quotation.setStatus(Quotation.QuotationStatus.valueOf(rs.getString("status")));
        quotation.setDescription(rs.getString("description"));
        quotation.setSubtotal(rs.getBigDecimal("subtotal"));
        quotation.setTaxAmount(rs.getBigDecimal("tax_amount"));
        quotation.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        quotation.setTotalAmount(rs.getBigDecimal("total_amount"));
        quotation.setCurrency(rs.getString("currency"));
        quotation.setNotes(rs.getString("notes"));
        quotation.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
        quotation.setCreatedBy(rs.getString("created_by"));
        
        if (rs.getTimestamp("last_modified") != null) {
            quotation.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
        }
        quotation.setLastModifiedBy(rs.getString("last_modified_by"));
        
        quotation.setConvertedToInvoice(rs.getBoolean("converted_to_invoice"));
        quotation.setConvertedInvoiceNumber(rs.getString("converted_invoice_number"));
        if (rs.getDate("conversion_date") != null) {
            quotation.setConversionDate(rs.getDate("conversion_date").toLocalDate());
        }
        
        return quotation;
    }
    
    /**
     * Quotation statistics
     */
    public static class QuotationStats {
        public int totalQuotations;
        public BigDecimal totalAmount;
        public BigDecimal pendingAmount;
        public BigDecimal acceptedAmount;
        public int convertedToInvoices;
        
        public QuotationStats() {
            this.totalAmount = BigDecimal.ZERO;
            this.pendingAmount = BigDecimal.ZERO;
            this.acceptedAmount = BigDecimal.ZERO;
        }
    }
    
    /**
     * Get quotation statistics
     */
    public QuotationStats getQuotationStats() throws SQLException {
        if (dbManager == null) {
            throw new SQLException("Database connection not available - running in limited mode");
        }
        
        QuotationStats stats = new QuotationStats();
        
        String sql = "SELECT " +
                    "COUNT(*) as total_quotations, " +
                    "COALESCE(SUM(total_amount), 0) as total_amount, " +
                    "COALESCE(SUM(CASE WHEN status IN ('SENT', 'VIEWED') THEN total_amount ELSE 0 END), 0) as pending_amount, " +
                    "COALESCE(SUM(CASE WHEN status = 'ACCEPTED' THEN total_amount ELSE 0 END), 0) as accepted_amount, " +
                    "COUNT(CASE WHEN converted_to_invoice = 1 THEN 1 END) as converted_count " +
                    "FROM quotations";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.totalQuotations = rs.getInt("total_quotations");
                stats.totalAmount = rs.getBigDecimal("total_amount");
                stats.pendingAmount = rs.getBigDecimal("pending_amount");
                stats.acceptedAmount = rs.getBigDecimal("accepted_amount");
                stats.convertedToInvoices = rs.getInt("converted_count");
            }
        }
        
        return stats;
    }
}
