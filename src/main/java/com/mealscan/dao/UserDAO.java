package com.mealscan.dao;

import com.mealscan.config.DatabaseInitializer;
import com.mealscan.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {
    
    public User createUser(String email, String password, String name, User.UserRole role) throws Exception {
        String sql = "INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, name);
            pstmt.setString(4, role.name());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt(1));
                    user.setEmail(email);
                    user.setName(name);
                    user.setRole(role);
                    return user;
                }
            }
        }
        throw new Exception("Failed to create user");
    }
    
    public User findByEmail(String email) throws Exception {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        }
        return null;
    }
    
    public User findById(int id) throws Exception {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUser(rs);
                }
            }
        }
        return null;
    }
    
    public User authenticate(String email, String password) throws Exception {
        User user = findByEmail(email);
        
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    
    public List<User> getAllStudents() throws Exception {
        String sql = "SELECT * FROM users WHERE role = 'STUDENT' ORDER BY name";
        List<User> students = new ArrayList<>();
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                students.add(extractUser(rs));
            }
        }
        return students;
    }
    
    public List<User> getAllUsers() throws Exception {
        String sql = "SELECT * FROM users ORDER BY role, name";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(extractUser(rs));
            }
        }
        return users;
    }
    
    public List<User> getUsersByRole(User.UserRole role) throws Exception {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY name";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, role.name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUser(rs));
                }
            }
        }
        return users;
    }
    
    public boolean emailExists(String email) throws Exception {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    public boolean deleteUser(int userId) throws Exception {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    public boolean deleteUserWithRecords(int userId) throws Exception {
        String deleteRecordsSql = "DELETE FROM records WHERE student_id = ? OR contractor_id = ?";
        String deleteUserSql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteRecordsSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(deleteUserSql)) {
                
                pstmt1.setInt(1, userId);
                pstmt1.setInt(2, userId);
                pstmt1.executeUpdate();
                
                pstmt2.setInt(1, userId);
                int affectedRows = pstmt2.executeUpdate();
                
                conn.commit();
                return affectedRows > 0;
                
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    
    public boolean updateUser(int userId, String name, String email) throws Exception {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setInt(3, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    public boolean changeUserRole(int userId, User.UserRole newRole) throws Exception {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newRole.name());
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    public boolean resetPassword(int userId, String newPassword) throws Exception {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        User user = findById(userId);
        if (user == null) {
            throw new Exception("User not found");
        }
        
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new Exception("Current password is incorrect");
        }
        
        return resetPassword(userId, newPassword);
    }
    
    public List<User> searchUsers(String searchTerm) throws Exception {
        String sql = "SELECT * FROM users WHERE name LIKE ? OR email LIKE ? ORDER BY name";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUser(rs));
                }
            }
        }
        return users;
    }
    
    public Map<String, Object> getUserStats() throws Exception {
        String sql = "SELECT role, COUNT(*) as count FROM users GROUP BY role";
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            int totalUsers = 0;
            while (rs.next()) {
                String role = rs.getString("role");
                int count = rs.getInt("count");
                stats.put(role.toLowerCase() + "Count", count);
                totalUsers += count;
            }
            stats.put("totalUsers", totalUsers);
        }
        return stats;
    }
    
    public boolean userHasRecords(int userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM records WHERE student_id = ? OR contractor_id = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    public Map<String, Integer> getUserRecordCounts(int userId) throws Exception {
        String studentSql = "SELECT COUNT(*) FROM records WHERE student_id = ?";
        String contractorSql = "SELECT COUNT(*) FROM records WHERE contractor_id = ?";
        
        Map<String, Integer> counts = new HashMap<>();
        
        try (Connection conn = DatabaseInitializer.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(studentSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        counts.put("studentRecords", rs.getInt(1));
                    }
                }
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(contractorSql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        counts.put("contractorRecords", rs.getInt(1));
                    }
                }
            }
        }
        
        counts.put("totalRecords", counts.getOrDefault("studentRecords", 0) + counts.getOrDefault("contractorRecords", 0));
        return counts;
    }
    
    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}