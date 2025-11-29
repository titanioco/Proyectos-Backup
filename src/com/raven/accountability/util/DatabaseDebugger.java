package com.raven.accountability.util;

import com.raven.accountability.database.DatabaseManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple utility to debug database content
 */
public class DatabaseDebugger {
    
    public static void main(String[] args) {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            System.out.println("=== EXISTING INVOICE NUMBERS ===");
            String sql = "SELECT invoice_number, customer_id, invoice_date, status FROM invoices ORDER BY invoice_number";
            ResultSet rs = dbManager.executeQuery(sql);
            
            int count = 0;
            while (rs.next()) {
                System.out.println(String.format("%s | Customer: %d | Date: %s | Status: %s", 
                    rs.getString("invoice_number"),
                    rs.getLong("customer_id"),
                    rs.getString("invoice_date"),
                    rs.getString("status")
                ));
                count++;
            }
            rs.close();
            
            System.out.println("\nTotal invoices: " + count);
            
            System.out.println("\n=== CUSTOMERS ===");
            String customerSql = "SELECT customer_id, company_name FROM customers ORDER BY customer_id";
            ResultSet customerRs = dbManager.executeQuery(customerSql);
            
            int customerCount = 0;
            while (customerRs.next()) {
                System.out.println(String.format("ID: %d | Company: %s", 
                    customerRs.getLong("customer_id"),
                    customerRs.getString("company_name")
                ));
                customerCount++;
            }
            customerRs.close();
            
            System.out.println("\nTotal customers: " + customerCount);
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
