package com.mealscan.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealscan.dao.RecordDAO;
import com.mealscan.dao.UserDAO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class UserStatsServlet extends HttpServlet {
    
    private final UserDAO userDAO = new UserDAO();
    private final RecordDAO recordDAO = new RecordDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, "Unauthorized", 401);
            return;
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"MESS_CONTRACTOR".equals(userRole) && !"CANTEEN_CONTRACTOR".equals(userRole)) {
            sendError(resp, "Only contractors can view stats", 403);
            return;
        }
        
        try {
            int contractorId = (int) session.getAttribute("userId");
            
            Map<String, Object> stats = new HashMap<>();
            
            // Basic user stats
            Map<String, Object> userStats = userDAO.getUserStats();
            stats.putAll(userStats);
            
            // Contractor-specific stats
            var records = recordDAO.getRecordsByContractor(contractorId);
            var students = userDAO.getAllStudents();
            
            // Calculate various statistics
            stats.put("totalRecords", records.size());
            stats.put("totalStudents", students.size());
            
            // Today's records
            long todayRecords = records.stream()
                .filter(r -> r.getRecordDate().equals(LocalDate.now()))
                .count();
            stats.put("todayRecords", todayRecords);
            
            // Monthly earnings
            BigDecimal monthlyEarnings = records.stream()
                .filter(r -> r.getRecordDate().getMonth() == LocalDate.now().getMonth())
                .map(r -> BigDecimal.valueOf(r.getCost().doubleValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("monthlyEarnings", monthlyEarnings);
            
            // Total earnings
            BigDecimal totalEarnings = records.stream()
                .map(r -> BigDecimal.valueOf(r.getCost().doubleValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalEarnings", totalEarnings);
            
            // Average meal cost
            double avgMealCost = records.isEmpty() ? 0 : 
                totalEarnings.doubleValue() / records.size();
            stats.put("avgMealCost", BigDecimal.valueOf(avgMealCost));
            
            // Recent activity (last 5 records)
            var recentActivity = records.stream()
                .limit(5)
                .map(r -> Map.of(
                    "studentName", r.getStudentName(),
                    "mealType", r.getMealType(),
                    "cost", r.getCost(),
                    "date", r.getRecordDate().toString()
                ))
                .toList();
            stats.put("recentActivity", recentActivity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            resp.setStatus(200);
            objectMapper.writeValue(resp.getWriter(), response);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Failed to load statistics: " + e.getMessage(), 500);
        }
    }
    
    private void sendError(HttpServletResponse resp, String message, int status) throws IOException {
        resp.setStatus(status);
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        objectMapper.writeValue(resp.getWriter(), error);
    }
}