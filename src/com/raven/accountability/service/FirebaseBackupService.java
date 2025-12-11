package com.raven.accountability.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.raven.accountability.model.Invoice;
import com.raven.accountability.model.Customer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.time.format.DateTimeFormatter;

/**
 * Service for handling Firebase Cloud backups using Firebase Admin SDK.
 * Provides real cloud backup and restore functionality for invoices.
 */
public class FirebaseBackupService {
    
    private static final String SERVICE_ACCOUNT_KEY_PATH = "backup-contable-firebase-adminsdk-fbsvc-55f4d4ba75.json";
    private static final String COLLECTION_NAME = "invoices";
    
    private boolean isInitialized = false;
    private String projectId;
    private Firestore firestore;
    
    public FirebaseBackupService() {
        try {
            initializeFirebase();
        } catch (Exception e) {
            System.err.println("FIREBASE: Failed to initialize Firebase service: " + e.getMessage());
            e.printStackTrace();
            this.isInitialized = false;
        }
    }
    
    /**
     * Initialize Firebase Admin SDK with service account credentials
     */
    private void initializeFirebase() throws IOException {
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_KEY_PATH);
                
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
                
                FirebaseApp.initializeApp(options);
                System.out.println("FIREBASE: Firebase App initialized successfully");
            } else {
                System.out.println("FIREBASE: Using existing Firebase App instance");
            }
            
            // Get Firestore instance
            this.firestore = FirestoreClient.getFirestore();
            this.projectId = "backup-contable";
            this.isInitialized = true;
            
            System.out.println("FIREBASE: Service initialized for project: " + projectId);
            System.out.println("FIREBASE: Firestore connection established");
            
        } catch (IOException e) {
            System.err.println("FIREBASE: Error reading service account key: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("FIREBASE: Error initializing Firebase: " + e.getMessage());
            throw new IOException("Firebase initialization failed", e);
        }
    }
    
    /**
     * Backs up a list of invoices to Firebase Cloud Firestore.
     * @param invoices The list of invoices to backup.
     * @return A CompletableFuture that completes when the backup is finished.
     */
    public CompletableFuture<Boolean> backupInvoices(List<Invoice> invoices) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isInitialized) {
                System.err.println("FIREBASE: Service not initialized, cannot backup");
                return false;
            }
            
            try {
                System.out.println("FIREBASE: Starting backup of " + invoices.size() + " invoices...");
                int successCount = 0;
                int failCount = 0;
                
                for (Invoice invoice : invoices) {
                    try {
                        // Convert invoice to Firestore document
                        Map<String, Object> invoiceData = invoiceToMap(invoice);
                        
                        // Use invoice number as document ID for easy retrieval
                        String documentId = invoice.getInvoiceNumber().replace("/", "_");
                        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(documentId);
                        
                        // Write to Firestore
                        WriteResult result = docRef.set(invoiceData).get();
                        successCount++;
                        
                        if (successCount % 5 == 0) {
                            System.out.println("FIREBASE: Uploaded " + successCount + "/" + invoices.size() + " invoices...");
                        }
                        
                    } catch (Exception e) {
                        failCount++;
                        System.err.println("FIREBASE: Failed to backup invoice " + invoice.getInvoiceNumber() + ": " + e.getMessage());
                    }
                }
                
                System.out.println("FIREBASE: Backup completed - Success: " + successCount + ", Failed: " + failCount);
                return failCount == 0;
                
            } catch (Exception e) {
                System.err.println("FIREBASE: Backup failed: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    /**
     * Restores invoices from Firebase Cloud Firestore.
     * @return A CompletableFuture containing the list of restored invoices.
     */
    public CompletableFuture<List<Invoice>> restoreInvoices() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isInitialized) {
                System.err.println("FIREBASE: Service not initialized, cannot restore");
                return new ArrayList<>();
            }
            
            try {
                System.out.println("FIREBASE: Connecting to cloud storage...");
                List<Invoice> invoices = new ArrayList<>();
                
                // Query all documents from the invoices collection
                List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();
                
                System.out.println("FIREBASE: Fetching " + documents.size() + " invoices from cloud...");
                
                for (QueryDocumentSnapshot document : documents) {
                    try {
                        Invoice invoice = mapToInvoice(document.getData());
                        invoices.add(invoice);
                    } catch (Exception e) {
                        System.err.println("FIREBASE: Failed to parse invoice from document " + document.getId() + ": " + e.getMessage());
                    }
                }
                
                System.out.println("FIREBASE: Successfully restored " + invoices.size() + " invoices from cloud");
                return invoices;
                
            } catch (Exception e) {
                System.err.println("FIREBASE: Restore failed: " + e.getMessage());
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * Convert Invoice object to Firestore-compatible Map
     */
    private Map<String, Object> invoiceToMap(Invoice invoice) {
        Map<String, Object> data = new HashMap<>();
        
        // Basic invoice fields
        data.put("invoiceNumber", invoice.getInvoiceNumber());
        data.put("invoiceDate", invoice.getInvoiceDate().toString());
        data.put("dueDate", invoice.getDueDate().toString());
        data.put("status", invoice.getStatus().name());
        data.put("description", invoice.getDescription());
        
        // Customer information
        if (invoice.getCustomer() != null) {
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("customerId", invoice.getCustomer().getCustomerId());
            customerData.put("companyName", invoice.getCustomer().getCompanyName());
            customerData.put("contactPerson", invoice.getCustomer().getContactPerson());
            customerData.put("email", invoice.getCustomer().getEmail());
            customerData.put("phone", invoice.getCustomer().getPhone());
            data.put("customer", customerData);
        }
        
        // Financial fields
        data.put("subtotal", invoice.getSubtotal() != null ? invoice.getSubtotal().toString() : "0");
        data.put("taxAmount", invoice.getTaxAmount() != null ? invoice.getTaxAmount().toString() : "0");
        data.put("discountAmount", invoice.getDiscountAmount() != null ? invoice.getDiscountAmount().toString() : "0");
        data.put("totalAmount", invoice.getTotalAmount() != null ? invoice.getTotalAmount().toString() : "0");
        data.put("paidAmount", invoice.getPaidAmount() != null ? invoice.getPaidAmount().toString() : "0");
        data.put("balanceAmount", invoice.getBalanceAmount() != null ? invoice.getBalanceAmount().toString() : "0");
        
        // Metadata
        data.put("currency", invoice.getCurrency());
        data.put("notes", invoice.getNotes());
        data.put("createdBy", invoice.getCreatedBy());
        data.put("backupTimestamp", System.currentTimeMillis());
        
        return data;
    }
    
    /**
     * Convert Firestore Map to Invoice object
     */
    private Invoice mapToInvoice(Map<String, Object> data) {
        Invoice invoice = new Invoice();
        
        // Basic fields
        invoice.setInvoiceNumber((String) data.get("invoiceNumber"));
        invoice.setInvoiceDate(java.time.LocalDate.parse((String) data.get("invoiceDate")));
        invoice.setDueDate(java.time.LocalDate.parse((String) data.get("dueDate")));
        invoice.setStatus(Invoice.InvoiceStatus.valueOf((String) data.get("status")));
        invoice.setDescription((String) data.get("description"));
        
        // Customer (simplified - would need CustomerService to fully restore)
        @SuppressWarnings("unchecked")
        Map<String, Object> customerData = (Map<String, Object>) data.get("customer");
        if (customerData != null) {
            Customer customer = new Customer();
            customer.setCustomerId(((Number) customerData.get("customerId")).longValue());
            customer.setCompanyName((String) customerData.get("companyName"));
            customer.setContactPerson((String) customerData.get("contactPerson"));
            customer.setEmail((String) customerData.get("email"));
            customer.setPhone((String) customerData.get("phone"));
            invoice.setCustomer(customer);
        }
        
        // Financial fields
        invoice.setSubtotal(new java.math.BigDecimal((String) data.get("subtotal")));
        invoice.setTaxAmount(new java.math.BigDecimal((String) data.get("taxAmount")));
        invoice.setDiscountAmount(new java.math.BigDecimal((String) data.get("discountAmount")));
        invoice.setTotalAmount(new java.math.BigDecimal((String) data.get("totalAmount")));
        invoice.setPaidAmount(new java.math.BigDecimal((String) data.get("paidAmount")));
        invoice.setBalanceAmount(new java.math.BigDecimal((String) data.get("balanceAmount")));
        
        // Metadata
        invoice.setCurrency((String) data.get("currency"));
        invoice.setNotes((String) data.get("notes"));
        invoice.setCreatedBy((String) data.get("createdBy"));
        
        return invoice;
    }
    
    /**
     * Check if Firebase service is available and initialized
     */
    public boolean isServiceAvailable() {
        return isInitialized && firestore != null;
    }
    
    /**
     * Get the project ID
     */
    public String getProjectId() {
        return projectId;
    }
}
