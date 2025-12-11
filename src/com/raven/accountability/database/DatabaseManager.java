

package com.raven.accountability.database;

import java.sql.*;
import java.io.File;

/**
 * Database Manager for Business Accountability System
 * Handles database connections and initialization with enhanced thread safety for SQLite
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DEFAULT_DB_NAME = "accountability.db";
    private static final String DB_URL = "jdbc:sqlite:" + DEFAULT_DB_NAME;
    private boolean initialized = false;
    
    // Thread-local connections for SQLite thread safety
    private static final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();
    
    // Singleton pattern
    private DatabaseManager() {
        // Constructor left empty - initialization happens on demand
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Get database connection with thread-local storage for SQLite safety
     */
    public Connection getConnection() throws SQLException {
        // Check if we have a valid thread-local connection
        Connection conn = threadLocalConnection.get();
        
        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
            // Create new connection for this thread
            conn = createNewConnection();
            threadLocalConnection.set(conn);
            System.out.println("DATABASE: Created new thread-local connection for thread: " + Thread.currentThread().getName());
        }
        
        return conn;
    }
    
    /**
     * Create a new SQLite connection with proper configuration
     */
    private Connection createNewConnection() throws SQLException {
        try {
            System.out.println("DATABASE: Creating new SQLite connection...");
            
            // Ensure database is initialized
            if (!initialized) {
                initializeDatabase();
            }
            
            // Try to create the database file directory if it doesn't exist
            File dbFile = new File(DEFAULT_DB_NAME);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Configure SQLite connection with threading support
            String url = DB_URL + "?journal_mode=WAL&synchronous=NORMAL&cache_size=10000&temp_store=memory&foreign_keys=on&busy_timeout=30000";
            
            Connection connection;
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(url);  
                System.out.println("DATABASE: SQLite connection created successfully");
            } catch (ClassNotFoundException e) {
                System.out.println("DATABASE: Explicit driver loading failed, trying direct connection...");
                connection = DriverManager.getConnection(url);
            }
            
            // Test the connection
            if (!connection.isValid(5)) {
                throw new SQLException("Database connection is not valid");
            }
            
            // Configure SQLite settings for this connection
            configureSQLiteConnection(connection);
            
            return connection;
            
        } catch (SQLException e) {
            System.err.println("DATABASE ERROR: Failed to create connection - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Initialize database and create tables (only runs once)
     */
    private synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }
        
        try {
            System.out.println("DATABASE: Initializing accountability database schema...");
            
            // Create a temporary connection just for initialization  
            Connection tempConnection = createTempConnection();
            
            // Configure SQLite
            configureSQLiteConnection(tempConnection);
            
            // Create tables
            createTables(tempConnection);
            
            // Insert default data
            insertDefaultData(tempConnection);
            
            // Close temporary connection
            tempConnection.close();
            
            initialized = true;
            System.out.println("DATABASE: Successfully initialized accountability database schema");
            
        } catch (SQLException e) {
            System.err.println("DATABASE ERROR: Failed to initialize database - " + e.getMessage());
            
            // Try in-memory database as last resort
            try {
                System.out.println("DATABASE: Attempting in-memory database as fallback...");
                Connection memConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
                configureSQLiteConnection(memConnection);
                createTables(memConnection);
                insertDefaultData(memConnection);
                memConnection.close();
                initialized = true;
                System.out.println("DATABASE: In-memory database initialized successfully");
            } catch (SQLException e2) {
                System.err.println("DATABASE CRITICAL ERROR: Failed to initialize even in-memory database");
                e2.printStackTrace();
                throw new RuntimeException("Cannot initialize database", e2);
            }
        }
    }
    
    /**
     * Create temporary connection for initialization
     */
    private Connection createTempConnection() throws SQLException {
        try {
            File dbFile = new File(DEFAULT_DB_NAME);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            String url = DB_URL + "?journal_mode=WAL&synchronous=NORMAL&cache_size=10000&temp_store=memory&foreign_keys=on&busy_timeout=30000";
            
            try {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection(url);
            } catch (ClassNotFoundException e) {
                return DriverManager.getConnection(url);
            }
        } catch (SQLException e) {
            System.err.println("DATABASE ERROR: Failed to create temporary connection - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Configure SQLite settings for optimal multithreading support
     */
    private void configureSQLiteConnection(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Enable foreign keys (essential for data integrity)
            stmt.execute("PRAGMA foreign_keys = ON");
            // Set WAL mode for better concurrency (already set in connection URL but ensure it's active)
            stmt.execute("PRAGMA journal_mode = WAL");
            // Set synchronous mode for better performance while maintaining safety
            stmt.execute("PRAGMA synchronous = NORMAL");
            // Set cache size for better performance
            stmt.execute("PRAGMA cache_size = 10000");
            // Use memory for temporary storage
            stmt.execute("PRAGMA temp_store = memory");
            // Set a reasonable timeout for busy database
            stmt.execute("PRAGMA busy_timeout = 30000");
            // Optimize for multithreading
            stmt.execute("PRAGMA threading = 1");
            // Set page size for better performance
            stmt.execute("PRAGMA page_size = 4096");
            
            System.out.println("DATABASE: SQLite configured successfully for multithreading");
            
            // Verify configuration
            ResultSet rs = stmt.executeQuery("PRAGMA journal_mode");
            if (rs.next()) {
                System.out.println("DATABASE: Journal mode: " + rs.getString(1));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("DATABASE WARNING: Failed to configure SQLite optimally - " + e.getMessage());
            // Continue anyway as these are optimization settings, but still enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA busy_timeout = 30000");
            } catch (SQLException e2) {
                System.err.println("DATABASE ERROR: Could not enable basic SQLite settings: " + e2.getMessage());
            }
        }
    }
    
    /**
     * Create database tables
     */
    private void createTables(Connection connection) throws SQLException {
        String[] createTableStatements = {
            // Customers table
            "CREATE TABLE IF NOT EXISTS customers (" +
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
            "tax_id VARCHAR(50)," +
            "status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "payment_terms VARCHAR(20) DEFAULT 'NET_30'," +
            "credit_limit DECIMAL(15,2)," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            
            // Invoices table
            "CREATE TABLE IF NOT EXISTS invoices (" +
            "invoice_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "invoice_number VARCHAR(50) UNIQUE NOT NULL," +
            "customer_id INTEGER NOT NULL," +
            "invoice_date DATE NOT NULL," +
            "due_date DATE NOT NULL," +
            "status VARCHAR(20) DEFAULT 'DRAFT'," +
            "description TEXT," +
            "subtotal DECIMAL(15,2) DEFAULT 0.00," +
            "tax_amount DECIMAL(15,2) DEFAULT 0.00," +
            "discount_amount DECIMAL(15,2) DEFAULT 0.00," +
            "total_amount DECIMAL(15,2) DEFAULT 0.00," +
            "paid_amount DECIMAL(15,2) DEFAULT 0.00," +
            "balance_amount DECIMAL(15,2) DEFAULT 0.00," +
            "currency VARCHAR(3) DEFAULT 'USD'," +
            "notes TEXT," +
            "last_payment_date DATE," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "created_by VARCHAR(255)," +
            "last_modified_by VARCHAR(255)," +
            "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
            ")",
            
            // Invoice Items table
            "CREATE TABLE IF NOT EXISTS invoice_items (" +
            "item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "invoice_id INTEGER NOT NULL," +
            "description TEXT NOT NULL," +
            "product_code VARCHAR(100)," +
            "quantity DECIMAL(10,4) NOT NULL," +
            "unit_price DECIMAL(15,2) NOT NULL," +
            "discount DECIMAL(15,2) DEFAULT 0.00," +
            "tax_rate DECIMAL(5,4) DEFAULT 0.0000," +
            "total DECIMAL(15,2) NOT NULL," +
            "unit VARCHAR(50) DEFAULT 'unit'," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE" +
            ")",
            
            // Tax Jurisdictions table
            "CREATE TABLE IF NOT EXISTS tax_jurisdictions (" +
            "jurisdiction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "jurisdiction_code VARCHAR(50) UNIQUE NOT NULL," +
            "jurisdiction_name VARCHAR(255) NOT NULL," +
            "jurisdiction_type VARCHAR(50) NOT NULL," +
            "country VARCHAR(100)," +
            "state VARCHAR(100)," +
            "city VARCHAR(100)," +
            "tax_rate DECIMAL(5,4) NOT NULL," +
            "tax_type VARCHAR(50)," +
            "is_active BOOLEAN DEFAULT 1," +
            "effective_date DATE," +
            "expiration_date DATE," +
            "description TEXT," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            
            // Quotations table
            "CREATE TABLE IF NOT EXISTS quotations (" +
            "quotation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "quotation_number VARCHAR(50) UNIQUE NOT NULL," +
            "customer_id INTEGER NOT NULL," +
            "quotation_date DATE NOT NULL," +
            "valid_until_date DATE NOT NULL," +
            "status VARCHAR(20) DEFAULT 'DRAFT'," +
            "description TEXT," +
            "subtotal DECIMAL(15,2) DEFAULT 0.00," +
            "tax_amount DECIMAL(15,2) DEFAULT 0.00," +
            "discount_amount DECIMAL(15,2) DEFAULT 0.00," +
            "total_amount DECIMAL(15,2) DEFAULT 0.00," +
            "currency VARCHAR(3) DEFAULT 'USD'," +
            "notes TEXT," +
            "converted_to_invoice BOOLEAN DEFAULT 0," +
            "converted_invoice_number VARCHAR(50)," +
            "conversion_date DATE," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "created_by VARCHAR(255)," +
            "last_modified_by VARCHAR(255)," +
            "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
            ")",
            
            // Quotation Items table
            "CREATE TABLE IF NOT EXISTS quotation_items (" +
            "item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "quotation_id INTEGER NOT NULL," +
            "description TEXT NOT NULL," +
            "quantity DECIMAL(10,4) NOT NULL," +
            "unit_price DECIMAL(15,2) NOT NULL," +
            "total DECIMAL(15,2) NOT NULL," +
            "notes TEXT," +
            "line_order INTEGER DEFAULT 0," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (quotation_id) REFERENCES quotations(quotation_id) ON DELETE CASCADE" +
            ")",
            
            // Company Settings table
            "CREATE TABLE IF NOT EXISTS company_settings (" +
            "setting_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "company_name VARCHAR(255)," +
            "company_address TEXT," +
            "company_phone VARCHAR(50)," +
            "company_email VARCHAR(255)," +
            "company_website VARCHAR(255)," +
            "company_tax_id VARCHAR(50)," +
            "company_logo_path VARCHAR(500)," +
            "default_currency VARCHAR(3) DEFAULT 'USD'," +
            "default_payment_terms VARCHAR(20) DEFAULT 'NET_30'," +
            "default_tax_rate DECIMAL(5,4) DEFAULT 0.0000," +
            "invoice_prefix VARCHAR(20) DEFAULT 'INV'," +
            "quotation_prefix VARCHAR(20) DEFAULT 'QUO'," +
            "next_invoice_number INTEGER DEFAULT 1," +
            "next_quotation_number INTEGER DEFAULT 1," +
            "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        };
        
        Statement stmt = connection.createStatement();
        for (String sql : createTableStatements) {
            stmt.execute(sql);
        }
        stmt.close();
        
        // Create indexes for better performance
        createIndexes(connection);
    }
    
    /**
     * Create database indexes
     */
    private void createIndexes(Connection connection) throws SQLException {
        String[] indexStatements = {
            "CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email)",
            "CREATE INDEX IF NOT EXISTS idx_customers_status ON customers(status)",
            "CREATE INDEX IF NOT EXISTS idx_invoices_customer ON invoices(customer_id)",
            "CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status)",
            "CREATE INDEX IF NOT EXISTS idx_invoices_date ON invoices(invoice_date)",
            "CREATE INDEX IF NOT EXISTS idx_invoices_due_date ON invoices(due_date)",
            "CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice ON invoice_items(invoice_id)",
            "CREATE INDEX IF NOT EXISTS idx_quotations_customer ON quotations(customer_id)",
            "CREATE INDEX IF NOT EXISTS idx_quotations_status ON quotations(status)",
            "CREATE INDEX IF NOT EXISTS idx_quotations_date ON quotations(quotation_date)",
            "CREATE INDEX IF NOT EXISTS idx_quotations_valid_until ON quotations(valid_until_date)",
            "CREATE INDEX IF NOT EXISTS idx_quotations_converted ON quotations(converted_to_invoice)",
            "CREATE INDEX IF NOT EXISTS idx_quotation_items_quotation ON quotation_items(quotation_id)",
            "CREATE INDEX IF NOT EXISTS idx_tax_jurisdictions_active ON tax_jurisdictions(is_active)",
            "CREATE INDEX IF NOT EXISTS idx_tax_jurisdictions_type ON tax_jurisdictions(jurisdiction_type)"
        };
        
        Statement stmt = connection.createStatement();
        for (String sql : indexStatements) {
            stmt.execute(sql);
        }
        stmt.close();
    }
    
    /**
     * Insert default data
     */
    private void insertDefaultData(Connection connection) throws SQLException {
        // Check if company settings exist
        String checkCompanySQL = "SELECT COUNT(*) FROM company_settings";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(checkCompanySQL);
        
        if (rs.next() && rs.getInt(1) == 0) {
            // Insert default company settings
            String insertCompanySQL = "INSERT INTO company_settings " +
                "(company_name, company_address, company_phone, company_email, " +
                " default_currency, default_payment_terms, default_tax_rate, " +
                " invoice_prefix, quotation_prefix, next_invoice_number, next_quotation_number) " +
                "VALUES " +
                "('Your Company Name', 'Your Company Address', 'Your Phone', 'your@email.com', " +
                " 'USD', 'NET_30', 0.10, 'INV', 'QUO', 1, 1)";
            stmt.execute(insertCompanySQL);
        }
        
        // Check if tax jurisdictions exist
        String checkTaxSQL = "SELECT COUNT(*) FROM tax_jurisdictions";
        rs = stmt.executeQuery(checkTaxSQL);
        
        if (rs.next() && rs.getInt(1) == 0) {
            // Insert default tax jurisdictions
            String[] taxInserts = {
                "INSERT INTO tax_jurisdictions (jurisdiction_code, jurisdiction_name, jurisdiction_type, country, tax_rate, tax_type, effective_date) VALUES ('US-FEDERAL', 'US Federal Tax', 'FEDERAL', 'United States', 0.0000, 'INCOME_TAX', '2024-01-01')",
                "INSERT INTO tax_jurisdictions (jurisdiction_code, jurisdiction_name, jurisdiction_type, country, tax_rate, tax_type, effective_date) VALUES ('US-SALES', 'US Sales Tax', 'STATE', 'United States', 8.2500, 'SALES_TAX', '2024-01-01')",
                "INSERT INTO tax_jurisdictions (jurisdiction_code, jurisdiction_name, jurisdiction_type, country, tax_rate, tax_type, effective_date) VALUES ('VAT-EU', 'EU VAT Standard', 'FEDERAL', 'European Union', 20.0000, 'VAT', '2024-01-01')"
            };
            
            for (String sql : taxInserts) {
                stmt.execute(sql);
            }
        }
        
        rs.close();
        stmt.close();
    }
    
    /**
     * Execute a query and return ResultSet
     */
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }
    
    /**
     * Execute an update statement
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        int result = pstmt.executeUpdate();
        pstmt.close();
        return result;
    }
    
    /**
     * Execute an insert and return generated key
     */
    public long executeInsert(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();
        long generatedId = 0;
        if (rs.next()) {
            generatedId = rs.getLong(1);
        }
        
        rs.close();
        pstmt.close();
        return generatedId;
    }
    
    /**
     * Close thread-local database connection
     */
    public void closeConnection() {
        try {
            Connection conn = threadLocalConnection.get();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                threadLocalConnection.remove();
                System.out.println("DATABASE: Thread-local connection closed successfully");
            }
        } catch (SQLException e) {
            System.err.println("DATABASE ERROR: Failed to close connection");
            e.printStackTrace();
        }
    }
    
    /**
     * Check if database connection is valid
     */
    public boolean isConnectionValid() {
        try {
            Connection conn = threadLocalConnection.get();
            return conn != null && !conn.isClosed() && conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
