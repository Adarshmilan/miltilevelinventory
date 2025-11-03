package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.PurchaseOrder;
import model.User;
import DAO.PurchaseOrderDAO;
import DAO.InventoryDAO;
import javax.servlet.http.HttpSession;
import java.util.*;


public class POServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        String role = (String)session.getAttribute("role");
        PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
        User user = (User)session.getAttribute("user");
        
        List<PurchaseOrder> poList = null;
        String targetPage = "dashboard_main.jsp"; 

        if (role.equals("Admin")) {
            poList = poDAO.getPendingPurchaseRequests();
            targetPage = "purchaseOrders.jsp";
        } else if (role.equals("Supplier")) {
            poList = poDAO.getApprovedPOsBySupplier(); 
            targetPage = "purchaseOrders.jsp";
        } else if (role.equals("WarehouseManager")) {
            InventoryDAO invDAO = new InventoryDAO();
            try {
                Integer warehouseId = invDAO.getWarehouseId(user.getUserId());
                if (warehouseId != null) {
                    poList = poDAO.getSentPOsByWarehouse(warehouseId);
                    targetPage = "purchaseOrders.jsp"; 
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            response.sendRedirect("dashboard.jsp?error=Access+Denied+to+PO+System.");
            return;
        }

        request.setAttribute("poList", poList); 
        request.setAttribute("currentRole", role);
        request.getRequestDispatcher(targetPage).forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String action = request.getParameter("action");
        
        if (session == null || session.getAttribute("user") == null) { 
            response.sendRedirect("login.jsp"); 
            return;
        }
        
        String role = (String)session.getAttribute("role");
        
        if ("createRequest".equals(action) && (role.equals("WarehouseManager") || role.equals("Admin"))) {
            handleCreateRequest(request, response, session, role);
        } else if (("approve".equals(action) || "reject".equals(action)) && role.equals("Admin")) {
            handleAdminAction(request, response, action);
        } else if (("confirm_delivery".equals(action) || "supplier_reject".equals(action)) && role.equals("Supplier")) {
            handleSupplierAction(request, response, action);
        } else if("receive_goods".equals(action) && (role.equals("WarehouseManager"))){
            handelreciveandconfirm(request, response);        
        }else {
            response.sendRedirect("dashboard.jsp?error=Unauthorized+Action+Attempted.");
        }
    }

    private void handleCreateRequest(HttpServletRequest request, HttpServletResponse response, HttpSession session, String role) 
            throws ServletException, IOException {
        
        User user = (User)session.getAttribute("user");
        String warehouseIdStr = "1";
        if(role.equals("Admin")){
            warehouseIdStr = "1";
        }else if(role.equals("WarehouseManager")){
            warehouseIdStr = request.getParameter("warehouseId");
        }
        else{ warehouseIdStr = request.getParameter("warehouseId");}
       
        
        String productIdStr = request.getParameter("productId");
        String qtyStr = request.getParameter("requestedQuantity");
        
        int warehouseId = 0, productId = 0, requestedQuantity = 0;
        try {
            if (warehouseIdStr == null || warehouseIdStr.isEmpty()) throw new NumberFormatException("WH ID missing.");
            if (productIdStr == null || productIdStr.isEmpty()) throw new NumberFormatException("Product ID missing.");
            if (qtyStr == null || qtyStr.isEmpty()) throw new NumberFormatException("Qty missing.");
            
            warehouseId = Integer.parseInt(warehouseIdStr);
            productId = Integer.parseInt(productIdStr);
            requestedQuantity = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("InventoryServlet?error=Invalid+form+data."+e); return;
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setWarehouseId(warehouseId);
        po.setProductId(productId);
        po.setRequestedQuantity(requestedQuantity);
        
        PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
        boolean success = poDAO.createPurchaseRequest(po);
        
        if (success) {
            response.sendRedirect("InventoryServlet?message=PO+request+submitted+for+approval!");
        } else {
            response.sendRedirect("InventoryServlet?error=Failed+to+create+PO+request.");
        }
    }

    private void handleAdminAction(HttpServletRequest request, HttpServletResponse response, String action) 
            throws IOException {
        
        String poIdStr = request.getParameter("poId");
        String newStatus = action.equals("approve") ? "Approved" : "Cancelled"; 

        int poId = 0;
        try { poId = Integer.parseInt(poIdStr); } catch (NumberFormatException e) {
            response.sendRedirect("POServlet?error=Invalid+PO+ID+Format."); return;
        }

        PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
        boolean success = poDAO.updatePOStatus(poId, newStatus); 

        if (success) {
            String message = (action.equals("approve")) 
                             ? "PO ID " + poId + " has been Approved and is ready to send to the Supplier."
                             : "PO ID " + poId + " has been Rejected.";
            response.sendRedirect("POServlet?message=" + message.replace(" ", "+"));
        } else {
            response.sendRedirect("POServlet?error=Failed+to+update+PO+status.");
        }
    }

    private void handleSupplierAction(HttpServletRequest request, HttpServletResponse response, String action) 
            throws IOException {
        
        String poIdStr = request.getParameter("poId");
        String newStatus = action.equals("confirm_delivery") ? "Sent" : "Cancelled"; 

        int poId = 0;
        try { poId = Integer.parseInt(poIdStr); } catch (NumberFormatException e) {
            response.sendRedirect("POServlet?error=Invalid+PO+ID+Format."); return;
        }

        PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
        boolean success = poDAO.updatePOStatus(poId, newStatus); 

        if (success) {
            String message = (action.equals("confirm_delivery")) 
                             ? "PO ID " + poId + " Confirmed by Supplier. Status updated to SENT."
                             : "PO ID " + poId + " Rejected by Supplier.";
            
            response.sendRedirect("POServlet?message=" + message.replace(" ", "+"));
        } else {
            response.sendRedirect("POServlet?error=Failed+to+update+PO+status.");
        }
    }

    private void handelreciveandconfirm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String poIdStr = request.getParameter("poId");
        String warehouseIdStr = request.getParameter("warehouseid");
        int warehouseId;
        int productId;
        
        try {
            if (warehouseIdStr == null || warehouseIdStr.isEmpty()) throw new NumberFormatException("WH ID missing fro confirmation");
            if (poIdStr == null || poIdStr.isEmpty()) throw new NumberFormatException("Product ID missing for conformation");
            
            warehouseId = Integer.parseInt(warehouseIdStr);
            productId = Integer.parseInt(poIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("InventoryServlet?error=Invalid+form+data."+e); return;
        }
        
        PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
        
        boolean comcom = poDAO.receiveGoodsAndUpdateStock(productId, warehouseId);
        
        if (comcom) {
            response.sendRedirect("InventoryServlet?message=Item+added+to+the+inventory");
        } else {
            response.sendRedirect("InventoryServlet?error=Failed+to+add+the+item");
        }
    }
}