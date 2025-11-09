package com.mealscan.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mealscan.dao.RecordDAO;
import com.mealscan.model.Record;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillServlet extends HttpServlet {
    
    private final RecordDAO recordDAO = new RecordDAO();
    private final ObjectMapper objectMapper;
    
    public BillServlet() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, "Unauthorized", 401);
            return;
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"STUDENT".equals(userRole)) {
            sendError(resp, "Only students can view bills", 403);
            return;
        }
        
        try {
            int studentId = (int) session.getAttribute("userId");
            String type = req.getParameter("type");
            String yearStr = req.getParameter("year");
            String monthStr = req.getParameter("month");
            
            if (type == null || type.isEmpty() || yearStr == null || monthStr == null) {
                sendError(resp, "Type, year, and month parameters are required", 400);
                return;
            }
            
            Record.RecordType recordType = Record.RecordType.valueOf(type.toUpperCase());
            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);
            
            if (month < 1 || month > 12) {
                sendError(resp, "Month must be between 1 and 12", 400);
                return;
            }
            
            List<Record> records = recordDAO.getMonthlyBill(studentId, recordType, year, month);
            
            BigDecimal total = records.stream()
                .map(Record::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records);
            response.put("total", total);
            response.put("month", month);
            response.put("year", year);
            response.put("type", type);
            
            resp.setStatus(200);
            objectMapper.writeValue(resp.getWriter(), response);
            
        } catch (IllegalArgumentException e) {
            sendError(resp, "Invalid parameters: " + e.getMessage(), 400);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Failed to generate bill: " + e.getMessage(), 500);
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