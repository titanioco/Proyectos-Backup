package com.raven.accountability.util;

import java.sql.*;
import java.io.File;

/**
 * Simple Database Initializer 
 * Creates and populates the accountability database with sample data
 */
public class SimpleDatabaseInitializer {
    
    private static final String DB_URL = "jdbc:sqlite:accountability.db";
    
    public static void main(String[] args) {
        System.out.println("🔧 Simple Database Initialization");
        System.out.println("=================================");
        
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create connection
            Connection conn = DriverManager.getConnection(DB_URL);
            
            System.out.println("✅ Database connection established");
            
            // Enable foreign keys
            Statement stmt = conn.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Create tables
            createTables(conn);
            
            // Insert sample data
            insertSampleData(conn);
            
            // Verify database
            verifyDatabase(conn);
            
            stmt.close();
            conn.close();
            
            System.out.println("🎉 Database initialization completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Customers table
        stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
            "customer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "customer_code VARCHAR(50) UNIQUE NOT NULL," +
            "company_name VARCHAR(255) NOT NULL," +
            "contact_person VARCHAR(255)," +
            "email VARCHAR(255)," +
            "phone VARCHAR(50)," +
            "address TEXT," +
            "city VARCHAR(100)," +
            "state VARCHAR(100)," +
            "zip_code VARCHAR(20)," +
            "country VARCHAR(100)," +
            "status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "payment_terms VARCHAR(20) DEFAULT 'NET_30'," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");
            
        // Invoices table
        stmt.execute("CREATE TABLE IF NOT EXISTS invoices (" +
            "invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "invoice_number VARCHAR(50) UNIQUE NOT NULL," +
            "customer_id INTEGER NOT NULL," +
            "invoice_date DATE NOT NULL," +
            "due_date DATE NOT NULL," +
            "status VARCHAR(20) DEFAULT 'DRAFT'," +
            "description TEXT," +
            "subtotal DECIMAL(15,2) DEFAULT 0.00," +
            "tax_amount DECIMAL(15,2) DEFAULT 0.00," +
            "total_amount DECIMAL(15,2) DEFAULT 0.00," +
            "paid_amount DECIMAL(15,2) DEFAULT 0.00," +
            "balance_amount DECIMAL(15,2) DEFAULT 0.00," +
            "currency VARCHAR(3) DEFAULT 'USD'," +
            "notes TEXT," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
            ")");
            
        // Tax jurisdictions table  
        stmt.execute("CREATE TABLE IF NOT EXISTS tax_jurisdictions (" +
            "jurisdiction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "jurisdiction_name VARCHAR(255) NOT NULL," +
            "jurisdiction_type VARCHAR(50) NOT NULL," +
            "tax_rate DECIMAL(8,6) NOT NULL," +
            "is_active BOOLEAN DEFAULT 1," +
            "effective_date DATE," +
            "description TEXT," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");
            
        stmt.close();
        System.out.println("✅ Database tables created");
    }
    
    private static void insertSampleData(Connection conn) throws SQLException {
        // Insert sample customers
        PreparedStatement customerStmt = conn.prepareStatement(
            "INSERT INTO customers (customer_code, company_name, contact_person, email, phone, city, state, country) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            
        String[][] customers = {
            {"ACME001", "Acme Corporation", "John Smith", "john@acme.com", "555-0101", "Los Angeles", "CA", "USA"},
            {"TECH002", "TechStart Solutions", "Sarah Johnson", "sarah@techstart.com", "555-0102", "San Francisco", "CA", "USA"},
            {"GLOB003", "Global Industries", "Mike Wilson", "mike@global.com", "555-0103", "New York", "NY", "USA"},
            {"INNO004", "Innovation Labs", "Lisa Chen", "lisa@innovation.com", "555-0104", "Austin", "TX", "USA"},
            {"DIGI005", "Digital Dynamics", "Robert Brown", "robert@digital.com", "555-0105", "Seattle", "WA", "USA"}
        };
        
        for (String[] customer : customers) {
            for (int i = 0; i < customer.length; i++) {
                customerStmt.setString(i + 1, customer[i]);
            }
            customerStmt.executeUpdate();
        }
        customerStmt.close();
        System.out.println("✅ Inserted " + customers.length + " customers");
        
        // Insert sample invoices
        PreparedStatement invoiceStmt = conn.prepareStatement(
            "INSERT INTO invoices (invoice_number, customer_id, invoice_date, due_date, status, subtotal, tax_amount, total_amount, balance_amount, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
        Object[][] invoices = {
            {"INV-2025-001", 1, "2025-07-15", "2025-08-14", "SENT", 2287.04, 162.96, 2450.00, 2450.00, "Professional services"},
            {"INV-2025-002", 2, "2025-07-18", "2025-08-17", "PAID", 1750.46, 125.04, 1875.50, 0.00, "Software development"},
            {"INV-2025-003", 3, "2025-07-20", "2025-08-19", "OVERDUE", 2982.41, 218.34, 3200.75, 2200.75, "Consulting services"},
            {"INV-2025-004", 4, "2025-07-22", "2025-08-21", "VIEWED", 885.42, 64.83, 950.25, 950.25, "Research and analysis"},
            {"INV-2025-005", 5, "2025-07-25", "2025-08-24", "PARTIALLY_PAID", 3814.81, 285.19, 4100.00, 2050.00, "System integration"}
        };
        
        for (Object[] invoice : invoices) {
            for (int i = 0; i < invoice.length; i++) {
                invoiceStmt.setObject(i + 1, invoice[i]);
            }
            invoiceStmt.executeUpdate();
        }
        invoiceStmt.close();
        System.out.println("✅ Inserted " + invoices.length + " invoices");
        
        // Insert sample tax jurisdictions
        PreparedStatement taxStmt = conn.prepareStatement(
            "INSERT INTO tax_jurisdictions (jurisdiction_name, jurisdiction_type, tax_rate, is_active, effective_date, description) VALUES (?, ?, ?, ?, ?, ?)");
            
        Object[][] taxes = {
            {"California State", "STATE", 0.0725, true, "2025-01-01", "California state sales tax"},
            {"New York State", "STATE", 0.0800, true, "2025-01-01", "New York state sales tax"},
            {"Texas State", "STATE", 0.0625, true, "2025-01-01", "Texas state sales tax"},
            {"Federal Income", "FEDERAL", 0.2200, true, "2025-01-01", "Federal income tax bracket"}
        };
        
        for (Object[] tax : taxes) {
            for (int i = 0; i < tax.length; i++) {
                taxStmt.setObject(i + 1, tax[i]);
            }
            taxStmt.executeUpdate();
        }
        taxStmt.close();
        System.out.println("✅ Inserted " + taxes.length + " tax jurisdictions");
    }
    
    private static void verifyDatabase(Connection conn) throws SQLException {
        System.out.println("\n📊 Database Verification:");
        System.out.println("=========================");
        
        String[] tables = {"customers", "invoices", "tax_jurisdictions"};
        
        for (String table : tables) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
            if (rs.next()) {
                System.out.println("📋 " + table + ": " + rs.getInt(1) + " records");
            }
            rs.close();
            stmt.close();
        }
        
        // Check file size
        File dbFile = new File("accountability.db");
        if (dbFile.exists()) {
            long size = dbFile.length();
            System.out.println("💾 Database file size: " + (size / 1024) + " KB");
        }
    }
}
