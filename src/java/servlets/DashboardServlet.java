/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlets;

import DAO.DashboardDAO;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.User;

/**
 *
 * @author ADARSH MILAN
 */
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User user = (User)session.getAttribute("user");
        String role = (String)session.getAttribute("role");
        
        DashboardDAO dashDAO = new DashboardDAO();
        
        try {
            // 1. Get Core Metrics (Total Products, Warehouses, etc.)
            request.setAttribute("coreMetrics", dashDAO.getCoreMetrics());
            
            // 2. Get Role-Specific Alerts
            if ("WarehouseManager".equals(role)) {
                Integer whId = (Integer)session.getAttribute("warehouseId");
                if (whId != null) {
                    request.setAttribute("lowStockItems", dashDAO.getLowStockAlerts(whId));
                    request.setAttribute("pendingPO", dashDAO.getPendingPOsCount());
                }
            } else if ("Admin".equals(role)) {
                request.setAttribute("lowStockItems", dashDAO.getLowStockAlertsforadmin());
                request.setAttribute("pendingPO", dashDAO.getPendingPOsCount());
                // Admin might see overall metrics
            }
            
            // Forward to the dashboard content fragment
            request.getRequestDispatcher("dashboard_main.jsp").forward(request, response);
            
        } catch (SQLException ex) {
            response.getWriter().write("<div style='color:red;'>Error loading dashboard metrics: " + ex.getMessage() + "</div>");
        }
    }
}
