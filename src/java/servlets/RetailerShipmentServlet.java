
package servlets;

import DAO.InventoryDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Product;
import model.RetailShipment;

public class RetailerShipmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleView(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("issue_stock".equals(action)) {
            handleStockIssuance(request, response);
        } else {
            handleView(request, response);
        }
    }

    private void handleView(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null ) {
            response.sendRedirect("login.jsp"); 
            return;
        }

        Integer warehouseId = (Integer)session.getAttribute("warehouseId");
        if("Admin".equals(session.getAttribute("role"))){
            warehouseId = 1;
        }
        InventoryDAO invDAO = new InventoryDAO();        
        
        try {
            // 1. Data for the Issuance Form (All stock currently in WH)
            List<Product> availableStock = invDAO.getAvailableStockForShipment(warehouseId);
            request.setAttribute("availableStock", availableStock);
            
            // 2. Data for the List of Existing Shipments
            List<RetailShipment> shipments = invDAO.getRetailShipmentsByWarehouse(warehouseId);
            request.setAttribute("shipmentsList", shipments);

            // Forward to the new JSP page
            request.getRequestDispatcher("retailerShipments.jsp").forward(request, response);
            
        } catch (SQLException ex) {
            session.setAttribute("flashError", "Database error loading shipment data.");
            response.sendRedirect("dashboard_main.jsp");
        }
    }
    
    private void handleStockIssuance(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Integer warehouseId = (Integer)session.getAttribute("warehouseId");

        // 1. Get Form Inputs
        String productIdStr = request.getParameter("productId");
        String quantityStr = request.getParameter("quantity");
        String retailerName = request.getParameter("retailerName");

        int productId = 0;
        int quantity = 0;

        try {
            productId = Integer.parseInt(productIdStr);
            quantity = Integer.parseInt(quantityStr);

            if (warehouseId == null || quantity <= 0 || retailerName == null || retailerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing required fields or invalid quantity.");
            }
        } catch (Exception e) {
            session.setAttribute("flashError", "Validation Error: Check all fields.");
            response.sendRedirect("RetailerShipmentServlet"); 
            return;
        }
        
        InventoryDAO invDAO = new InventoryDAO();
        try {
            boolean success = invDAO.issueStockToRetailer(warehouseId, productId, quantity, retailerName);
            
            if (success) {
                session.setAttribute("flashMessage", "Stock successfully issued to " + retailerName + "!");
            } else {
                session.setAttribute("flashError", "Issuance Failed: Insufficient stock or transaction error.");
            }
            
        } catch (Exception ex) {
            session.setAttribute("flashError", "DB Error during stock issuance.");
        }
        // PRG pattern: Redirect to GET method to refresh the list
        response.sendRedirect("RetailerShipmentServlet"); 
    }
}
