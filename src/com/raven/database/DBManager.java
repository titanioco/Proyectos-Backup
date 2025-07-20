package com.raven.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
    private static final String DB_URL = "jdbc:sqlite:university.db";
    private static Connection connection;
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Explicitly load the SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                createTables();
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found", e);
            }
        }
        return connection;
    }
    
    private static void createTables() throws SQLException {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT, " +
                "google_sub TEXT, " +
                "full_name TEXT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
        }
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
