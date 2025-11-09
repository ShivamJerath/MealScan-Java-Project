package com.mealscan.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealscan.dao.UserDAO;
import com.mealscan.model.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterServlet extends HttpServlet {
    
    private final UserDAO userDAO = new UserDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> regData = objectMapper.readValue(req.getReader(), Map.class);
            String email = regData.get("email");
            String password = regData.get("password");
            String name = regData.get("name");
            String role = regData.get("role");
            
            if (email == null || email.trim().isEmpty() ||
                password == null || password.length() < 6 ||
                name == null || name.trim().isEmpty() ||
                role == null || role.trim().isEmpty()) {
                sendError(resp, "All fields are required. Password must be at least 6 characters", 400);
                return;
            }
            
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                sendError(resp, "Invalid email format", 400);
                return;
            }
            
            if (userDAO.emailExists(email)) {
                sendError(resp, "Email already registered", 409);
                return;
            }
            
            User.UserRole userRole;
            try {
                userRole = User.UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                sendError(resp, "Invalid role. Must be STUDENT, MESS_CONTRACTOR, or CANTEEN_CONTRACTOR", 400);
                return;
            }
            
            User user = userDAO.createUser(email, password, name, userRole);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "role", user.getRole().name()
            ));
            
            resp.setStatus(201);
            objectMapper.writeValue(resp.getWriter(), response);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Registration failed: " + e.getMessage(), 500);
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