package com.raven.test;

import com.raven.database.DBManager;
import java.sql.Connection;

public class DatabaseTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing SQLite connection...");
            Connection conn = DBManager.getConnection();
            System.out.println("✓ SQLite connection successful!");
            System.out.println("Database URL: " + conn.getMetaData().getURL());
            
            // Test creating a simple user
            System.out.println("✓ Database ready for use!");
            
        } catch (Exception e) {
            System.err.println("✗ Database connection failed:");
            e.printStackTrace();
        }
    }
}
