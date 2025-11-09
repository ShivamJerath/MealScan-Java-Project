package com.mealscan.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealscan.dao.RecordDAO;
import com.mealscan.model.Record;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class UploadRecordServlet extends HttpServlet {
    
    private final RecordDAO recordDAO = new RecordDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, "Unauthorized", 401);
            return;
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"MESS_CONTRACTOR".equals(userRole) && !"CANTEEN_CONTRACTOR".equals(userRole)) {
            sendError(resp, "Only contractors can upload records", 403);
            return;
        }
        
        try {
            int contractorId = (int) session.getAttribute("userId");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(req.getReader(), Map.class);
            
            Integer studentId = (Integer) data.get("studentId");
            String mealType = (String) data.get("mealType");
            String items = (String) data.get("items");
            Object costObj = data.get("cost");
            String recordDateStr = (String) data.get("recordDate");
            
            if (studentId == null || mealType == null || mealType.trim().isEmpty() ||
                items == null || items.trim().isEmpty() || costObj == null || recordDateStr == null) {
                sendError(resp, "All fields are required", 400);
                return;
            }
            
            BigDecimal cost;
            if (costObj instanceof Number) {
                cost = BigDecimal.valueOf(((Number) costObj).doubleValue());
            } else {
                cost = new BigDecimal(costObj.toString());
            }
            
            if (cost.compareTo(BigDecimal.ZERO) <= 0) {
                sendError(resp, "Cost must be greater than 0", 400);
                return;
            }
            
            LocalDate recordDate = LocalDate.parse(recordDateStr);
            
            Record.RecordType type = "MESS_CONTRACTOR".equals(userRole) ? 
                Record.RecordType.MESS : Record.RecordType.CANTEEN;
            
            Record record = new Record(studentId, contractorId, type, mealType, items, cost, recordDate);
            record = recordDAO.createRecord(record);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Record uploaded successfully");
            response.put("record", record);
            
            resp.setStatus(201);
            objectMapper.writeValue(resp.getWriter(), response);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Failed to upload record: " + e.getMessage(), 500);
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