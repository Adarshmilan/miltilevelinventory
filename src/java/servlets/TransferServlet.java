package servlets;

import DAO.*;
import model.*;
import java.util.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;

public class TransferServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleView(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String action = request.getParameter("action");
        String role = (String)session.getAttribute("role");

        // General Manager Authorization Check for POST actions
        if (session == null || !("WarehouseManager".equals(role))) {
            session.setAttribute("flashError", "Access Denied: Only Managers can manage transfers.");
            response.sendRedirect("login.jsp"); 
            return;
        }

        if ("request_transfer".equals(action)) {
            handleNewTransferRequest(request, response);
        } else if ("approve_transfer".equals(action)) {
            handleTransferApproval(request, response);
        } else if ("receive_transfer".equals(action)) {
            handleTransferReceipt(request, response);
        } else {
            handleView(request, response); // Fallback to view
        }
    }
    
    // --- View Handling (GET) ---
    private void handleView(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String role = (String)session.getAttribute("role");
        
        // Authorization Check
        if (session == null ) {
            response.sendRedirect("login.jsp"); 
            return;
        }
        
        Integer whId = (Integer)session.getAttribute("warehouseId");
        
        TransferDAO transferDAO = new TransferDAO();
        InventoryDAO invDAO = new InventoryDAO();
        
        try {
            // 1. Data for the Request Form (Stock from Manager's WH)
            List<Product> availableStock = null;
            if (whId != null) {
                availableStock = invDAO.getInventoryByWarehouse(whId);
            }
            
            // 2. Data for the Lists (All warehouses for destination dropdown)
            List<Warehouse> allWarehouses = transferDAO.getAllWarehouses();
            
            // 3. Data for the Pending/Active Transfer Table
            List<StockTransfer> transfers = transferDAO.getTransferRequests();

            request.setAttribute("availableStock", availableStock);
            request.setAttribute("allWarehouses", allWarehouses);
            request.setAttribute("transfers", transfers);
            
            // Forward to the JSP view
            request.getRequestDispatcher("stockTransfer.jsp").forward(request, response);
            
        } catch (SQLException ex) {
            session.setAttribute("flashError", "Database error loading transfer data: " + ex.getMessage());
            response.sendRedirect("dashboard_main.jsp");
        }
    }

    // --- Action 1: Create New Request (POST) ---
    private void handleNewTransferRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Integer fromWh = (Integer)session.getAttribute("warehouseId");
        String role = (String)session.getAttribute("role");
        
        if(role.equals("Admin")){
            fromWh = 1;
        }
        
        String fromWhStr = request.getParameter("whereWh");
        String toWhStr = request.getParameter("toWh");
        String productIdStr = request.getParameter("productId");
        String qtyStr = request.getParameter("quantity");
        
        int fromWhsel = 0;
        int toWh = 0;
        int productId = 0;
        int quantity = 0;
        
        try {
            fromWhsel = Integer.parseInt(fromWhStr);
            toWh = Integer.parseInt(toWhStr);
            productId = Integer.parseInt(productIdStr);
            quantity = Integer.parseInt(qtyStr);

            if (fromWh == null || fromWh.intValue() == toWh) {
                 session.setAttribute("flashError", "Invalid source/destination warehouse. Cannot transfer to self.");
                 response.sendRedirect("TransferServlet"); return;
            }
        } catch (Exception e) {
            session.setAttribute("flashError", "Validation Error: Invalid numeric input.");
            response.sendRedirect("TransferServlet"); 
            return;
        }

        try {
            StockTransfer transfer = new StockTransfer();
            transfer.setFromWh(fromWhsel);
            transfer.setToWh(toWh);
            transfer.setProductId(productId);
            transfer.setQuantity(quantity);
            
            TransferDAO transferDAO = new TransferDAO();
            boolean success = transferDAO.createTransferRequest(transfer);
            
            if (success) {
                session.setAttribute("flashMessage", "Transfer request submitted for approval!");
            } else {
                session.setAttribute("flashError", "Transfer request failed to be saved in the database.");
            }
            
        } catch (SQLException ex) {
            session.setAttribute("flashError", "DB Error: Could not save transfer request.");
        }
        response.sendRedirect("TransferServlet");
    }
    
    // --- Action 2: Approve & Ship (POST) ---
    private void handleTransferApproval(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Integer managerWhId = (Integer)session.getAttribute("warehouseId");

        String transferIdStr = request.getParameter("transferId");
        final int transferId;
        
        try { transferId = Integer.parseInt(transferIdStr); } catch (Exception e) {
            session.setAttribute("flashError", "Invalid Transfer ID."); response.sendRedirect("TransferServlet"); return;
        }
        
        TransferDAO transferDAO = new TransferDAO();
        try {
            // Get transfer details (product/qty/wh) before transaction
            List<StockTransfer> transfers = transferDAO.getTransferRequests();
            StockTransfer transfer = transfers.stream()
                .filter(t -> "Requested".equals(t.getStatus()) && transferId == t.getTransferId())
                .findFirst()
                .orElse(null);
            
            if (transfer == null || !managerWhId.equals(transfer.getFromWh())) {
                session.setAttribute("flashError", "Approval failed: Request invalid or unauthorized.");
                response.sendRedirect("TransferServlet"); return;
            }
            
            // EXECUTE THE CORE TRANSACTION (Stock Debit & Status Update to 'In Transit')
            boolean success = transferDAO.executeTransfer(
                transferId, transfer.getFromWh(), transfer.getToWh(), transfer.getProductId(), transfer.getQuantity()
            );
            
            if (success) {
                session.setAttribute("flashMessage", "Transfer approved, stock debited, and set to 'In Transit'.");
            } else {
                session.setAttribute("flashError", "Transfer execution failed (Insufficient stock in source).");
            }
            
        } catch (Exception ex) {
            session.setAttribute("flashError", "Approval Failed: " + ex.getMessage());
        }
        response.sendRedirect("TransferServlet");
    }
    
    // --- Action 3: Mark Received (POST) ---
    private void handleTransferReceipt(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Integer managerWhId = (Integer)session.getAttribute("warehouseId");

        String transferIdStr = request.getParameter("transferId");
        final int transferId;
        
        try { transferId = Integer.parseInt(transferIdStr); } catch (Exception e) {
            session.setAttribute("flashError", "Invalid Receipt ID."); response.sendRedirect("TransferServlet"); return;
        }

        TransferDAO transferDAO = new TransferDAO();
        try {
            // Verify authorization and status before marking receipt
            List<StockTransfer> transfers = transferDAO.getTransferRequests();
            StockTransfer transfer = transfers.stream()
                .filter(t -> t.getTransferId() == transferId && "In Transit".equals(t.getStatus()))
                .findFirst()
                .orElse(null);

            if (transfer == null || !managerWhId.equals(transfer.getToWh())) {
                session.setAttribute("flashError", "Receipt failed: Not authorized or transfer not in transit.");
                response.sendRedirect("TransferServlet"); return;
            }

            // Update status to 'Received'
            boolean success = transferDAO.updateTransferStatus(transferId, "Received");
            
            if (success) {
                session.setAttribute("flashMessage", "Stock transfer marked as RECEIVED!");
            } else {
                 session.setAttribute("flashError", "Failed to mark transfer as received.");
            }
            
        } catch (SQLException ex) {
            session.setAttribute("flashError", "DB Error during transfer receipt.");
        }
        response.sendRedirect("TransferServlet");
    }
}
