package com.raven.accountability.service;

import com.raven.accountability.model.TaxJurisdiction;
import com.raven.accountability.database.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Tax Service - handles tax jurisdiction operations with enhanced error handling
 */
public class TaxService {
    private DatabaseManager dbManager;
    private boolean isInitialized = false;
    
    public TaxService() {
        try {
            System.out.println("TAX SERVICE: Initializing TaxService...");
            this.dbManager = DatabaseManager.getInstance();
            
            // Test database connection
            Connection testConnection = dbManager.getConnection();
            if (testConnection != null && testConnection.isValid(5)) {
                isInitialized = true;
                System.out.println("TAX SERVICE: Successfully initialized with database connection");
            } else {
                throw new SQLException("Database connection is not valid");
            }
            
        } catch (Exception e) {
            System.err.println("TAX SERVICE ERROR: Failed to initialize TaxService - " + e.getMessage());
            e.printStackTrace();
            this.dbManager = null;
            this.isInitialized = false;
        }
    }
    
    /**
     * Check if the service is properly initialized
     */
    public boolean isInitialized() {
        return isInitialized && dbManager != null;
    }
    
    public List<TaxJurisdiction> getAllTaxJurisdictions() throws SQLException {
        if (!isInitialized()) {
            throw new SQLException("TaxService is not properly initialized");
        }
        
        List<TaxJurisdiction> taxes = new ArrayList<>();
        String sql = "SELECT * FROM tax_jurisdictions ORDER BY jurisdiction_name";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                TaxJurisdiction tax = mapResultSetToTaxJurisdiction(rs);
                taxes.add(tax);
            }
        }
        
        return taxes;
    }
    
    public List<TaxJurisdiction> getActiveTaxJurisdictions() throws SQLException {
        List<TaxJurisdiction> taxes = new ArrayList<>();
        String sql = "SELECT * FROM tax_jurisdictions WHERE is_active = 1 ORDER BY jurisdiction_name";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                TaxJurisdiction tax = mapResultSetToTaxJurisdiction(rs);
                taxes.add(tax);
            }
        }
        
        return taxes;
    }
    
    public TaxJurisdiction getTaxJurisdictionById(Long id) throws SQLException {
        String sql = "SELECT * FROM tax_jurisdictions WHERE jurisdiction_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTaxJurisdiction(rs);
                }
            }
        }
        
        return null;
    }
    
    public void saveTaxJurisdiction(TaxJurisdiction tax) throws SQLException {
        if (tax.getId() == null) {
            insertTaxJurisdiction(tax);
        } else {
            updateTaxJurisdiction(tax);
        }
    }
    
    private void insertTaxJurisdiction(TaxJurisdiction tax) throws SQLException {
        String sql = "INSERT INTO tax_jurisdictions (jurisdiction_name, tax_type, tax_rate, " +
                    "is_active, effective_date, description) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setTaxJurisdictionParameters(stmt, tax);
            
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating tax jurisdiction failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tax.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating tax jurisdiction failed, no ID obtained.");
                }
            }
        }
    }
    
    private void updateTaxJurisdiction(TaxJurisdiction tax) throws SQLException {
        String sql = "UPDATE tax_jurisdictions SET jurisdiction_name = ?, tax_type = ?, " +
                    "tax_rate = ?, is_active = ?, effective_date = ?, description = ? WHERE jurisdiction_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setTaxJurisdictionParameters(stmt, tax);
            stmt.setLong(7, tax.getId());
            
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Updating tax jurisdiction failed, no rows affected.");
            }
        }
    }
    
    public void deleteTaxJurisdiction(Long id) throws SQLException {
        String sql = "DELETE FROM tax_jurisdictions WHERE jurisdiction_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Deleting tax jurisdiction failed, no rows affected.");
            }
        }
    }
    
    public List<TaxJurisdiction> searchTaxJurisdictions(String searchTerm) throws SQLException {
        List<TaxJurisdiction> taxes = new ArrayList<>();
        String sql = "SELECT * FROM tax_jurisdictions WHERE " +
                    "jurisdiction_name LIKE ? OR tax_type LIKE ? OR description LIKE ? " +
                    "ORDER BY jurisdiction_name";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TaxJurisdiction tax = mapResultSetToTaxJurisdiction(rs);
                    taxes.add(tax);
                }
            }
        }
        
        return taxes;
    }
    
    public List<TaxJurisdiction> getTaxJurisdictionsByType(String taxType) throws SQLException {
        List<TaxJurisdiction> taxes = new ArrayList<>();
        String sql = "SELECT * FROM tax_jurisdictions WHERE tax_type = ? ORDER BY jurisdiction_name";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, taxType);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TaxJurisdiction tax = mapResultSetToTaxJurisdiction(rs);
                    taxes.add(tax);
                }
            }
        }
        
        return taxes;
    }
    
    public BigDecimal calculateTax(BigDecimal amount, Long taxJurisdictionId) throws SQLException {
        TaxJurisdiction tax = getTaxJurisdictionById(taxJurisdictionId);
        if (tax == null || !tax.isActive()) {
            return BigDecimal.ZERO;
        }
        
        return amount.multiply(tax.getTaxRate().divide(new BigDecimal("100")));
    }
    
    public int getTotalTaxJurisdictionsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tax_jurisdictions";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    public int getActiveTaxJurisdictionsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tax_jurisdictions WHERE is_active = 1";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    public BigDecimal getAverageTaxRate() throws SQLException {
        String sql = "SELECT AVG(tax_rate) FROM tax_jurisdictions WHERE is_active = 1";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                BigDecimal average = rs.getBigDecimal(1);
                return average != null ? average : BigDecimal.ZERO;
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    private void setTaxJurisdictionParameters(PreparedStatement stmt, TaxJurisdiction tax) throws SQLException {
        stmt.setString(1, tax.getJurisdictionName());
        stmt.setString(2, tax.getTaxType());
        stmt.setBigDecimal(3, tax.getTaxRate());
        stmt.setBoolean(4, tax.isActive());
        stmt.setDate(5, Date.valueOf(tax.getEffectiveDate()));
        stmt.setString(6, tax.getDescription());
    }
    
    private TaxJurisdiction mapResultSetToTaxJurisdiction(ResultSet rs) throws SQLException {
        TaxJurisdiction tax = new TaxJurisdiction();
        tax.setJurisdictionId(rs.getLong("jurisdiction_id"));
        tax.setJurisdictionCode(rs.getString("jurisdiction_code"));
        tax.setJurisdictionName(rs.getString("jurisdiction_name"));
        tax.setJurisdictionType(rs.getString("jurisdiction_type"));
        tax.setCountry(rs.getString("country"));
        tax.setState(rs.getString("state"));
        tax.setCity(rs.getString("city"));
        tax.setTaxRate(rs.getBigDecimal("tax_rate"));
        tax.setTaxType(rs.getString("tax_type"));
        tax.setActive(rs.getBoolean("is_active"));
        
        // Handle date parsing safely
        String effectiveDateStr = rs.getString("effective_date");
        if (effectiveDateStr != null && !effectiveDateStr.trim().isEmpty()) {
            try {
                tax.setEffectiveDate(java.time.LocalDate.parse(effectiveDateStr));
            } catch (Exception e) {
                System.err.println("TAX WARNING: Could not parse effective_date '" + effectiveDateStr + "', using current date");
                tax.setEffectiveDate(java.time.LocalDate.now());
            }
        } else {
            tax.setEffectiveDate(java.time.LocalDate.now());
        }
        
        // Handle expiration date
        String expirationDateStr = rs.getString("expiration_date");
        if (expirationDateStr != null && !expirationDateStr.trim().isEmpty()) {
            try {
                tax.setExpirationDate(java.time.LocalDate.parse(expirationDateStr));
            } catch (Exception e) {
                System.err.println("TAX WARNING: Could not parse expiration_date '" + expirationDateStr + "'");
                // Leave expiration date as null
            }
        }
        
        tax.setDescription(rs.getString("description"));
        
        // Handle timestamps safely
        try {
            java.sql.Timestamp createdTs = rs.getTimestamp("created_date");
            if (createdTs != null) {
                tax.setCreatedDate(createdTs.toLocalDateTime());
            }
        } catch (Exception e) {
            tax.setCreatedDate(java.time.LocalDateTime.now());
        }
        
        try {
            java.sql.Timestamp modifiedTs = rs.getTimestamp("last_modified");
            if (modifiedTs != null) {
                tax.setLastModified(modifiedTs.toLocalDateTime());
            }
        } catch (Exception e) {
            tax.setLastModified(java.time.LocalDateTime.now());
        }
        
        return tax;
    }
}
