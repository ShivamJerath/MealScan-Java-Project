package com.mealscan.config;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class DatabaseInitializer {
    
    private static final String DB_URL = "jdbc:h2:./mealscan;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    
    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            createTables(conn);
            insertDefaultData(conn);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private static void createTables(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS records (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    student_id INT NOT NULL,
                    contractor_id INT NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    meal_type VARCHAR(50) NOT NULL,
                    items TEXT NOT NULL,
                    cost DECIMAL(10, 2) NOT NULL,
                    record_date DATE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (contractor_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);
            
            System.out.println("Database tables created successfully");
        }
    }
    
    private static void insertDefaultData(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Default data already exists");
                return;
            }
        }
        
        String hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt());
        String insertUser = "INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertUser)) {
            pstmt.setString(1, "student@mealscan.com");
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, "John Doe");
            pstmt.setString(4, "STUDENT");
            pstmt.executeUpdate();
            
            pstmt.setString(1, "mess@mealscan.com");
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, "Mess Contractor");
            pstmt.setString(4, "MESS_CONTRACTOR");
            pstmt.executeUpdate();
            
            pstmt.setString(1, "canteen@mealscan.com");
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, "Canteen Contractor");
            pstmt.setString(4, "CANTEEN_CONTRACTOR");
            pstmt.executeUpdate();
            
            System.out.println("Default users created successfully");
        }
    }
    
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}