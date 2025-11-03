package servlets;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import DAO.InventoryDAO;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import model.User;
import model.Product;
import java.util.*;

/**
 *
 * @author ADARSH MILAN
 */
public class InventoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        String role = (String) session.getAttribute("role");
        
        if(role.equals("WarehouseManager")){
            User user = (User)session.getAttribute("user");
            int userId = user.getUserId();
            InventoryDAO inventoryDAO = new InventoryDAO();
            try {
                Integer warehouseId = inventoryDAO.getWarehouseId(userId);
                if (warehouseId == null) {
                    request.setAttribute("error", "Error: Manager not linked to a warehouse.");
                    request.getRequestDispatcher("dashboard_main.jsp").forward(request, response);
                    return;
                }
                
                List<Product> inventoryList = inventoryDAO.getInventoryByWarehouse(warehouseId);

                request.setAttribute("inventoryList", inventoryList);
                request.setAttribute("warehouseId", warehouseId); 

                request.getRequestDispatcher("inventory.jsp").include(request, response);

            } catch (SQLException ex) {
                request.setAttribute("error", "Database error occurred: " + ex.getMessage());
                request.getRequestDispatcher("dashboard.jsp").forward(request, response);
            }        
        }
        if(role.equals("Admin")){
            InventoryDAO inventoryDAO = new InventoryDAO();          
                
            List<Product> inventoryList = inventoryDAO.getallinventoryitems();

            request.setAttribute("inventoryList", inventoryList);

            request.getRequestDispatcher("inventory.jsp").include(request, response);

             
        }
        
    }

    
    private void handleGoodsReceipt(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User user = (User)session.getAttribute("user");
        int userId = user.getUserId();

        String poIdStr = request.getParameter("poId");
        int poId = 0;
        
        try { poId = Integer.parseInt(poIdStr); } catch (NumberFormatException e) {
            response.sendRedirect("dashboard.jsp?error=Invalid+PO+ID+for+receipt."); return;
        }
        
        InventoryDAO inventoryDAO = new InventoryDAO();
        try {
            Integer warehouseId = inventoryDAO.getWarehouseId(userId);
            if (warehouseId == null) {
                response.sendRedirect("dashboard.jsp?error=Manager+WH+Link+Missing."); return;
            }
            
            // CRITICAL TRANSACTION CALL
            boolean success = inventoryDAO.receiveGoodsAndUpdateStock(poId, warehouseId);
            
            if (success) {
                response.sendRedirect("InventoryServlet?message=Goods+Received+and+Inventory+Updated+for+PO+ID+" + poId);
            } else {
                response.sendRedirect("InventoryServlet?error=Failed+to+process+goods+receipt+transaction.");
            }
            
        } catch (SQLException ex) {
            response.sendRedirect("InventoryServlet?error=DB+Error+during+Goods+Receipt.");
        }
    }
}