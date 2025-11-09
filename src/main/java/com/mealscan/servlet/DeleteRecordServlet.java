package com.mealscan.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealscan.dao.RecordDAO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DeleteRecordServlet extends HttpServlet {
    
    private final RecordDAO recordDAO = new RecordDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, "Unauthorized", 401);
            return;
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"MESS_CONTRACTOR".equals(userRole) && !"CANTEEN_CONTRACTOR".equals(userRole)) {
            sendError(resp, "Only contractors can delete records", 403);
            return;
        }
        
        try {
            int contractorId = (int) session.getAttribute("userId");
            String recordIdStr = req.getParameter("id");
            
            if (recordIdStr == null || recordIdStr.isEmpty()) {
                sendError(resp, "Record ID is required", 400);
                return;
            }
            
            int recordId = Integer.parseInt(recordIdStr);
            
            boolean deleted = recordDAO.deleteRecord(recordId, contractorId);
            
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Record deleted successfully");
                
                resp.setStatus(200);
                objectMapper.writeValue(resp.getWriter(), response);
            } else {
                sendError(resp, "Record not found or you don't have permission to delete it", 404);
            }
            
        } catch (NumberFormatException e) {
            sendError(resp, "Invalid record ID", 400);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Failed to delete record: " + e.getMessage(), 500);
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