package com.mealscan.dao;

import com.mealscan.config.DatabaseInitializer;
import com.mealscan.model.Record;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordDAO {
    
    public Record createRecord(Record record) throws Exception {
        String sql = "INSERT INTO records (student_id, contractor_id, type, meal_type, items, cost, record_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, record.getStudentId());
            pstmt.setInt(2, record.getContractorId());
            pstmt.setString(3, record.getType().name());
            pstmt.setString(4, record.getMealType());
            pstmt.setString(5, record.getItems());
            pstmt.setBigDecimal(6, record.getCost());
            pstmt.setDate(7, Date.valueOf(record.getRecordDate()));
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setId(rs.getInt(1));
                    return record;
                }
            }
        }
        throw new Exception("Failed to create record");
    }
    
    public List<Record> getRecordsByStudentAndType(int studentId, Record.RecordType type) throws Exception {
        String sql = """
            SELECT r.*, u.name as student_name, c.name as contractor_name
            FROM records r
            JOIN users u ON r.student_id = u.id
            JOIN users c ON r.contractor_id = c.id
            WHERE r.student_id = ? AND r.type = ?
            ORDER BY r.record_date DESC, r.created_at DESC
        """;
        
        List<Record> records = new ArrayList<>();
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setString(2, type.name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(extractRecord(rs));
                }
            }
        }
        return records;
    }
    
    public List<Record> getRecordsByContractor(int contractorId) throws Exception {
        String sql = """
            SELECT r.*, u.name as student_name, c.name as contractor_name
            FROM records r
            JOIN users u ON r.student_id = u.id
            JOIN users c ON r.contractor_id = c.id
            WHERE r.contractor_id = ?
            ORDER BY r.record_date DESC, r.created_at DESC
        """;
        
        List<Record> records = new ArrayList<>();
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, contractorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(extractRecord(rs));
                }
            }
        }
        return records;
    }
    
    public List<Record> getMonthlyBill(int studentId, Record.RecordType type, int year, int month) throws Exception {
        String sql = """
            SELECT r.*, u.name as student_name, c.name as contractor_name
            FROM records r
            JOIN users u ON r.student_id = u.id
            JOIN users c ON r.contractor_id = c.id
            WHERE r.student_id = ? AND r.type = ? 
            AND YEAR(r.record_date) = ? AND MONTH(r.record_date) = ?
            ORDER BY r.record_date ASC
        """;
        
        List<Record> records = new ArrayList<>();
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setString(2, type.name());
            pstmt.setInt(3, year);
            pstmt.setInt(4, month);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(extractRecord(rs));
                }
            }
        }
        return records;
    }
    
    public boolean deleteRecord(int recordId, int contractorId) throws Exception {
        String sql = "DELETE FROM records WHERE id = ? AND contractor_id = ?";
        
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, recordId);
            pstmt.setInt(2, contractorId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    private Record extractRecord(ResultSet rs) throws SQLException {
        Record record = new Record();
        record.setId(rs.getInt("id"));
        record.setStudentId(rs.getInt("student_id"));
        record.setContractorId(rs.getInt("contractor_id"));
        record.setType(Record.RecordType.valueOf(rs.getString("type")));
        record.setMealType(rs.getString("meal_type"));
        record.setItems(rs.getString("items"));
        record.setCost(rs.getBigDecimal("cost"));
        record.setRecordDate(rs.getDate("record_date").toLocalDate());
        record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        record.setStudentName(rs.getString("student_name"));
        record.setContractorName(rs.getString("contractor_name"));
        return record;
    }
}