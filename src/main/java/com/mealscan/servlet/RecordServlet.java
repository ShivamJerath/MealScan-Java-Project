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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordServlet extends HttpServlet {
    
    private final RecordDAO recordDAO = new RecordDAO();
    private final ObjectMapper objectMapper;
    
    public RecordServlet() {
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
        
        try {
            int userId = (int) session.getAttribute("userId");
            String userRole = (String) session.getAttribute("userRole");
            String type = req.getParameter("type");
            
            List<Record> records;
            
            if ("STUDENT".equals(userRole)) {
                if (type == null || type.isEmpty()) {
                    sendError(resp, "Type parameter required (MESS or CANTEEN)", 400);
                    return;
                }
                Record.RecordType recordType = Record.RecordType.valueOf(type.toUpperCase());
                records = recordDAO.getRecordsByStudentAndType(userId, recordType);
                
            } else if ("MESS_CONTRACTOR".equals(userRole) || "CANTEEN_CONTRACTOR".equals(userRole)) {
                records = recordDAO.getRecordsByContractor(userId);
                
            } else {
                sendError(resp, "Invalid role", 403);
                return;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("records", records);
            
            resp.setStatus(200);
            objectMapper.writeValue(resp.getWriter(), response);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Failed to fetch records: " + e.getMessage(), 500);
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