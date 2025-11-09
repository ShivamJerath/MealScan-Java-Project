package com.mealscan;

import com.mealscan.config.DatabaseInitializer;
import com.mealscan.servlet.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

public class MealScanApplication {
    
    private static final int PORT = 9090;
    
    public static void main(String[] args) {
        try {
            System.out.println("Initializing database...");
            DatabaseInitializer.initialize();
            System.out.println("Database initialized successfully!");
            
            Server server = new Server(PORT);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            context.setBaseResource(Resource.newClassPathResource("webapp"));
            context.setWelcomeFiles(new String[]{"index.html"});
            
            registerServlets(context);
            
            ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
            defaultServlet.setInitParameter("dirAllowed", "false");
            context.addServlet(defaultServlet, "/");
            
            server.setHandler(context);
            
            System.out.println("Starting MealScan server on port " + PORT + "...");
            server.start();
            System.out.println("===========================================");
            System.out.println("MealScan is running!");
            System.out.println("Access the application at: http://localhost:" + PORT);
            System.out.println("===========================================");
            System.out.println("\nDefault Credentials:");
            System.out.println("Student: student@mealscan.com / password123");
            System.out.println("Mess Contractor: mess@mealscan.com / password123");
            System.out.println("Canteen Contractor: canteen@mealscan.com / password123");
            System.out.println("===========================================");
            
            server.join();
            
        } catch (Exception e) {
            System.err.println("Failed to start MealScan application");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void registerServlets(ServletContextHandler context) {
        context.addServlet(LoginServlet.class, "/api/login");
        context.addServlet(RegisterServlet.class, "/api/register");
        context.addServlet(LogoutServlet.class, "/api/logout");
        context.addServlet(RecordServlet.class, "/api/records");
        context.addServlet(UploadRecordServlet.class, "/api/records/upload");
        context.addServlet(DeleteRecordServlet.class, "/api/records/delete");
        context.addServlet(BillServlet.class, "/api/bills");
        context.addServlet(UserServlet.class, "/api/users");
        context.addServlet(DeleteUserServlet.class, "/api/users/delete");
        context.addServlet(UpdateUserServlet.class, "/api/users/update");
        context.addServlet(UserStatsServlet.class, "/api/stats");
        
        System.out.println("All servlets registered successfully");
    }
}