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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserServlet extends HttpServlet {
    
    private final UserDAO userDAO = new UserDAO();
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
            sendError(resp, "Only contractors can view students list", 403);
            return;
        }
        
        try {
            List<User> students = userDAO.getAllStudents();
            
            List<Map<String, Object>> studentsList = students.stream()
                .map(user -> {
                    Map<String, Object> studentMap = new HashMap<>();
                    studentMap.put("id", user.getId());
                    studentMap.put("name", user.getName());
                    studentMap.put("email", user.getEmail());
                    return studentMap;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("students", studentsList);
            
            resp.setStatus(200);
            objectMapper.writeValue(resp.getWriter(), response);
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Failed to fetch students: " + e.getMessage(), 500);
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