package com.mealscan.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealscan.dao.UserDAO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateUserServlet extends HttpServlet {
    
    private final UserDAO userDAO = new UserDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, "Unauthorized", 401);
            return;
        }
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"MESS_CONTRACTOR".equals(userRole) && !"CANTEEN_CONTRACTOR".equals(userRole)) {
            sendError(resp, "Only contractors can update users", 403);
            return;
        }
        
        try {
            String userIdStr = req.getParameter("id");
            if (userIdStr == null || userIdStr.isEmpty()) {
                sendError(resp, "User ID is required", 400);
                return;
            }
            
            int userId = Integer.parseInt(userIdStr);
            
            @SuppressWarnings("unchecked")
            Map<String, String> data = objectMapper.readValue(req.getReader(), Map.class);
            String name = data.get("name");
            String email = data.get("email");
            
            if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
                sendError(resp, "Name and email are required", 400);
                return;
            }
            
            // Check if email already exists for other users
            var existingUser = userDAO.findByEmail(email);
            if (existingUser != null && existingUser.getId() != userId) {
                sendError(resp, "Email already registered by another user", 409);
                return;
            }
            
            boolean updated = userDAO.updateUser(userId, name, email);
            
            if (updated) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "User updated successfully");
                
                resp.setStatus(200);
                objectMapper.writeValue(resp.getWriter(), response);
            } else {
                sendError(resp, "User not found", 404);
            }
            
        } catch (NumberFormatException e) {
            sendError(resp, "Invalid user ID", 400);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Failed to update user: " + e.getMessage(), 500);
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