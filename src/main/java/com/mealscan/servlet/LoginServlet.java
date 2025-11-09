package com.mealscan.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealscan.dao.UserDAO;
import com.mealscan.model.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginServlet extends HttpServlet {
    
    private final UserDAO userDAO = new UserDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> loginData = objectMapper.readValue(req.getReader(), Map.class);
            String email = loginData.get("email");
            String password = loginData.get("password");
            
            if (email == null || email.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                sendError(resp, "Email and password are required", 400);
                return;
            }
            
            User user = userDAO.authenticate(email, password);
            
            if (user == null) {
                sendError(resp, "Invalid email or password", 401);
                return;
            }
            
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole().name());
            session.setAttribute("userName", user.getName());
            session.setMaxInactiveInterval(3600);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "role", user.getRole().name()
            ));
            
            resp.setStatus(200);
            objectMapper.writeValue(resp.getWriter(), response);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Login failed: " + e.getMessage(), 500);
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