package com.raven.accountability.util;

import com.raven.accountability.database.DatabaseManager;
import com.raven.accountability.service.*;
import com.raven.accountability.model.*;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.io.File;

/**
 * Database Initializer Utility
 * Ensures the accountability database is properly initialized with sample data
 */
public class DatabaseInitializer {
    
    public static void main(String[] args) {
        System.out.println("🔧 Database Initialization Utility");
        System.out.println("===================================");
        
        try {
            // Initialize database
            DatabaseManager dbManager = DatabaseManager.getInstance();
            Connection conn = dbManager.getConnection();
            
            System.out.println("✅ Database connection established");
            
            // Check if database has tables
            boolean hasData = checkExistingData(conn);
            
            if (!hasData) {
                System.out.println("📊 Initializing database with sample data...");
                initializeSampleData(conn);
                System.out.println("✅ Sample data initialization complete!");
            } else {
                System.out.println("📊 Database already contains data");
            }
            
            // Verify database state
            verifyDatabaseState(conn);
            
            conn.close();
            System.out.println("🎉 Database initialization completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean checkExistingData(Connection conn) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM customers";
        try (PreparedStatement stmt = conn.prepareStatement(checkQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("📈 Found " + count + " existing customers");
                return count > 0;
            }
        } catch (SQLException e) {
            System.out.println("⚠️  Tables may not exist yet, will create sample data");
            return false;
        }
        return false;
    }
    
    private static void initializeSampleData(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        
        try {
            // Insert sample customers
            insertSampleCustomers(conn);
            
            // Insert sample company settings
            insertCompanySettings(conn);
            
            // Insert sample tax jurisdictions
            insertSampleTaxJurisdictions(conn);
            
            // Insert sample invoices
            insertSampleInvoices(conn);
            
            conn.commit();
            System.out.println("✅ All sample data committed successfully");
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    private static void insertSampleCustomers(Connection conn) throws SQLException {
        String insertCustomer = "INSERT INTO customers (customer_code, company_name, contact_person, email, phone, address, city, state, zip_code, country, status, payment_terms) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Object[][] customerData = {
            {"ACME001", "Acme Corporation", "John Smith", "john@acme.com", "555-0101", "123 Business St", "Los Angeles", "CA", "90210", "USA", "ACTIVE", "NET_30"},
            {"TECH002", "TechStart Solutions", "Sarah Johnson", "sarah@techstart.com", "555-0102", "456 Innovation Ave", "San Francisco", "CA", "94107", "USA", "ACTIVE", "NET_15"},
            {"GLOB003", "Global Industries", "Mike Wilson", "mike@global.com", "555-0103", "789 Corporate Blvd", "New York", "NY", "10001", "USA", "ACTIVE", "NET_45"},
            {"INNO004", "Innovation Labs", "Lisa Chen", "lisa@innovation.com", "555-0104", "321 Research Dr", "Austin", "TX", "73301", "USA", "ACTIVE", "NET_30"},
            {"DIGI005", "Digital Dynamics", "Robert Brown", "robert@digital.com", "555-0105", "654 Tech Plaza", "Seattle", "WA", "98101", "USA", "ACTIVE", "NET_30"},
            {"SMAR006", "Smart Solutions Inc", "Emily Davis", "emily@smart.com", "555-0106", "987 Solution Way", "Denver", "CO", "80201", "USA", "ACTIVE", "NET_30"},
            {"FUTU007", "Future Systems", "David Miller", "david@future.com", "555-0107", "147 Tomorrow St", "Miami", "FL", "33101", "USA", "ACTIVE", "NET_15"}
        };
        
        try (PreparedStatement stmt = conn.prepareStatement(insertCustomer)) {
            for (Object[] customer : customerData) {
                for (int i = 0; i < customer.length; i++) {
                    stmt.setObject(i + 1, customer[i]);
                }
                stmt.executeUpdate();
            }
        }
        
        System.out.println("✅ Inserted " + customerData.length + " sample customers");
    }
    
    private static void insertCompanySettings(Connection conn) throws SQLException {
        String insertSettings = "INSERT INTO company_settings (company_name, company_address, company_email, company_phone, default_currency, invoice_prefix, next_invoice_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(insertSettings)) {
            stmt.setString(1, "Business Accountability Solutions");
            stmt.setString(2, "123 Main Street, Suite 100\nBusiness City, BC 12345");
            stmt.setString(3, "info@businessaccountability.com");
            stmt.setString(4, "555-BUSINESS");
            stmt.setString(5, "USD");
            stmt.setString(6, "INV");
            stmt.setInt(7, 8);
            stmt.executeUpdate();
        }
        
        System.out.println("✅ Inserted company settings");
    }
    
    private static void insertSampleTaxJurisdictions(Connection conn) throws SQLException {
        String insertTax = "INSERT INTO tax_jurisdictions (jurisdiction_name, jurisdiction_type, tax_rate, is_active, effective_date, description) VALUES (?, ?, ?, ?, ?, ?)";
        
        Object[][] taxData = {
            {"California State", "STATE", 0.0725, true, "2025-01-01", "California state sales tax"},
            {"Los Angeles County", "COUNTY", 0.0100, true, "2025-01-01", "LA County additional tax"},
            {"New York State", "STATE", 0.0800, true, "2025-01-01", "New York state sales tax"},
            {"Texas State", "STATE", 0.0625, true, "2025-01-01", "Texas state sales tax"},
            {"Federal Income", "FEDERAL", 0.2200, true, "2025-01-01", "Federal income tax bracket"},
            {"Miami-Dade County", "COUNTY", 0.0075, true, "2025-01-01", "Miami-Dade additional tax"},
            {"Seattle City", "CITY", 0.0360, true, "2025-01-01", "Seattle city business tax"},
            {"Special District A", "SPECIAL", 0.0250, false, "2024-01-01", "Special taxing district"}
        };
        
        try (PreparedStatement stmt = conn.prepareStatement(insertTax)) {
            for (Object[] tax : taxData) {
                stmt.setString(1, (String) tax[0]);
                stmt.setString(2, (String) tax[1]);
                stmt.setBigDecimal(3, new BigDecimal(tax[2].toString()));
                stmt.setBoolean(4, (Boolean) tax[3]);
                stmt.setString(5, (String) tax[4]);
                stmt.setString(6, (String) tax[5]);
                stmt.executeUpdate();
            }
        }
        
        System.out.println("✅ Inserted " + taxData.length + " sample tax jurisdictions");
    }
    
    private static void insertSampleInvoices(Connection conn) throws SQLException {
        // First get customer IDs
        String getCustomerIds = "SELECT customer_id, customer_code FROM customers ORDER BY customer_code";
        int[] customerIds = new int[7];
        int index = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(getCustomerIds);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next() && index < 7) {
                customerIds[index++] = rs.getInt("customer_id");
            }
        }
        
        String insertInvoice = "INSERT INTO invoices (invoice_number, customer_id, invoice_date, due_date, status, subtotal, tax_amount, total_amount, paid_amount, balance_amount, currency, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Object[][] invoiceData = {
            {"INV-2025-001", customerIds[0], "2025-07-15", "2025-08-14", "SENT", "2287.04", "162.96", "2450.00", "0.00", "2450.00", "USD", "Professional services rendered"},
            {"INV-2025-002", customerIds[1], "2025-07-18", "2025-08-17", "PAID", "1750.46", "125.04", "1875.50", "1875.50", "0.00", "USD", "Software development services"},
            {"INV-2025-003", customerIds[2], "2025-07-20", "2025-08-19", "OVERDUE", "2982.41", "218.34", "3200.75", "1000.00", "2200.75", "USD", "Consulting and implementation"},
            {"INV-2025-004", customerIds[3], "2025-07-22", "2025-08-21", "VIEWED", "885.42", "64.83", "950.25", "0.00", "950.25", "USD", "Research and analysis"},
            {"INV-2025-005", customerIds[4], "2025-07-25", "2025-08-24", "PARTIALLY_PAID", "3814.81", "285.19", "4100.00", "2050.00", "2050.00", "USD", "System integration services"},
            {"INV-2025-006", customerIds[5], "2025-07-28", "2025-08-27", "DRAFT", "1537.04", "113.76", "1650.80", "0.00", "1650.80", "USD", "Training and support"},
            {"INV-2025-007", customerIds[6], "2025-07-30", "2025-08-29", "SENT", "2694.07", "196.33", "2890.40", "0.00", "2890.40", "USD", "Custom development"}
        };
        
        try (PreparedStatement stmt = conn.prepareStatement(insertInvoice)) {
            for (Object[] invoice : invoiceData) {
                for (int i = 0; i < invoice.length; i++) {
                    if (i == 2 || i == 3) { // Date fields
                        stmt.setString(i + 1, (String) invoice[i]);
                    } else {
                        stmt.setObject(i + 1, invoice[i]);
                    }
                }
                stmt.executeUpdate();
            }
        }
        
        System.out.println("✅ Inserted " + invoiceData.length + " sample invoices");
    }
    
    private static void verifyDatabaseState(Connection conn) throws SQLException {
        System.out.println("\n📊 Database State Verification:");
        System.out.println("===============================");
        
        String[] tables = {"customers", "invoices", "tax_jurisdictions", "company_settings"};
        
        for (String table : tables) {
            String countQuery = "SELECT COUNT(*) FROM " + table;
            try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("📋 " + table + ": " + count + " records");
                }
            }
        }
        
        // Check database file size
        File dbFile = new File("accountability.db");
        if (dbFile.exists()) {
            long size = dbFile.length();
            System.out.println("💾 Database file size: " + (size / 1024) + " KB");
        }
    }
}
