package com.mealscan.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Record {
    private int id;
    private int studentId;
    private int contractorId;
    private RecordType type;
    private String mealType;
    private String items;
    private BigDecimal cost;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    private String studentName;
    private String contractorName;
    
    public enum RecordType {
        MESS,
        CANTEEN
    }
    
    public Record() {}
    
    public Record(int studentId, int contractorId, RecordType type, 
                  String mealType, String items, BigDecimal cost, LocalDate recordDate) {
        this.studentId = studentId;
        this.contractorId = contractorId;
        this.type = type;
        this.mealType = mealType;
        this.items = items;
        this.cost = cost;
        this.recordDate = recordDate;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public int getContractorId() {
        return contractorId;
    }
    
    public void setContractorId(int contractorId) {
        this.contractorId = contractorId;
    }
    
    public RecordType getType() {
        return type;
    }
    
    public void setType(RecordType type) {
        this.type = type;
    }
    
    public String getMealType() {
        return mealType;
    }
    
    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
    
    public String getItems() {
        return items;
    }
    
    public void setItems(String items) {
        this.items = items;
    }
    
    public BigDecimal getCost() {
        return cost;
    }
    
    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getContractorName() {
        return contractorName;
    }
    
    public void setContractorName(String contractorName) {
        this.contractorName = contractorName;
    }
    
    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", type=" + type +
                ", mealType='" + mealType + '\'' +
                ", cost=" + cost +
                ", recordDate=" + recordDate +
                '}';
    }
}