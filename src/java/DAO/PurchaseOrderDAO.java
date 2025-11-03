
package DAO;

/**
 *
 * @author ADARSH MILAN
 */

import model.PurchaseOrder;
import util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class PurchaseOrderDAO {

    
    
    private static final String INSERT_PO_SQL = 
            "INSERT INTO PurchaseOrders (warehouse_id, supplier_id, status) VALUES (?, ?, 'Pending')";
    private static final String INSERT_PO_ITEM_SQL = 
            "INSERT INTO PO_Items (po_id, product_id, qty, price) VALUES (?, ?, ?, ?)";

    // ye ho gaya 
    private static final String GET_PENDING_PO_SQL = "SELECT po.po_id, po.created_date, po.status, po.warehouse_id, w.name AS warehouse_name, poi.qty, p.product_id, p.name AS product_name FROM PurchaseOrders po JOIN Warehouses w ON po.warehouse_id = w.warehouse_id JOIN PO_Items poi ON po.po_id = poi.po_id JOIN Products p ON poi.product_id = p.product_id WHERE po.status = 'Pending' ORDER BY po.created_date DESC;";
    // ye ho gaya 
    private static final String GET_APPROVED_PO_SUPPLIER_SQL = "SELECT po.po_id, po.created_date, po.warehouse_id, w.name AS warehouse_name, poi.qty, p.product_id, p.name AS product_name, po.status FROM PurchaseOrders po JOIN Warehouses w ON po.warehouse_id = w.warehouse_id JOIN PO_Items poi ON po.po_id = poi.po_id  JOIN Products p ON poi.product_id = p.product_id WHERE po.status = 'Approved' AND po.supplier_id = 101 ORDER BY po.created_date ASC;";
    
    
    // ye ho gaya 
    private static final String GET_SENT_PO_MANAGER_SQL = "SELECT po.po_id, po.created_date, po.warehouse_id, w.name AS warehouse_name, po.status, poi.qty, p.product_id, p.name AS product_name FROM PurchaseOrders po JOIN Warehouses w ON po.warehouse_id = w.warehouse_id JOIN PO_Items poi ON po.po_id = poi.po_id JOIN Products p ON poi.product_id = p.product_id where po.warehouse_id = 1 ORDER BY po.status ASC;";

    // ye ho gaya 
    private static final String UPDATE_PO_STATUS_SQL ="UPDATE PurchaseOrders SET status = ? WHERE po_id = ?";
    
    
    
   //ye sb last update ke liye hai jb manager update karega
    private static final String GET_PO_ITEMS_SQL = 
        "SELECT product_id, qty FROM PO_Items WHERE po_id = ?";
    private static final String UPDATE_INVENTORY_SQL = 
        "INSERT INTO Inventory (warehouse_id, product_id, quantity) VALUES (?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
    private static final String UPDATE_PO_DELIVERED_SQL =
        "UPDATE PurchaseOrders SET status = 'Delivered' WHERE po_id = ?";

    // --- Core Read Method ---
    private List<PurchaseOrder> executePOQuery(PreparedStatement pstmt) throws SQLException {
        List<PurchaseOrder> poList = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                PurchaseOrder po = new PurchaseOrder();
                po.setPoId(rs.getInt("po_id"));
                po.setWarehouseId(rs.getInt("warehouse_id"));
                po.setWarehouseName(rs.getString("warehouse_name"));
                po.setRequestedQuantity(rs.getInt("qty"));
                po.setProductId(rs.getInt("product_id"));
                po.setProductName(rs.getString("product_name"));
                po.setStatus(rs.getString("status"));
                
                // Handle LocalDateTime conversion
                if (rs.getTimestamp("created_date") != null) {
                    po.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
                }
                poList.add(po);
            }
        }
        return poList;
    }
    
    // --- DAO Methods ---

    public boolean createPurchaseRequest(PurchaseOrder po) {
        Connection conn = null;
        boolean success = false;
        int defaultSupplierId = 101; // Linked to Global Supplier Inc. (user_id 3)

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); 
            int poId = insertPoHeader(conn, po.getWarehouseId(), defaultSupplierId);

            if (poId > 0) {
                insertPoItem(conn, poId, po.getProductId(), po.getRequestedQuantity());
                conn.commit(); 
                
                success = true;
            } else {
                conn.rollback(); 
            }
        } catch (SQLException e) {
            System.err.println("Transaction Failed during PO creation.");
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                DBUtil.close(conn);
            } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }
    
    // Helper to insert header and return ID
    private int insertPoHeader(Connection conn, int warehouseId, int supplierId) throws SQLException {
        int poId = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(INSERT_PO_SQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, warehouseId);
            pstmt.setInt(2, supplierId);
            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) { poId = rs.getInt(1); }
                }
            }
        }
        return poId;
    }
    
    // Helper to insert item
    private void insertPoItem(Connection conn, int poId, int productId, int qty) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(INSERT_PO_ITEM_SQL)) {
            pstmt.setInt(1, poId);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, qty);
            int price = qty * 83;
            pstmt.setInt(4,price);
            pstmt.executeUpdate();
        }
    }
    
    public boolean updatePOStatus(int poId, String newStatus) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PO_STATUS_SQL)) {
            
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, poId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error updating PO status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<PurchaseOrder> getPendingPurchaseRequests() {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(GET_PENDING_PO_SQL)) {
            return executePOQuery(pstmt);
        } catch (SQLException e) {
            e.printStackTrace(); return new ArrayList<>();
        }
    }
    
    public List<PurchaseOrder> getApprovedPOsBySupplier() {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(GET_APPROVED_PO_SUPPLIER_SQL)) {
            return executePOQuery(pstmt);
        } catch (SQLException e) {
            e.printStackTrace(); return new ArrayList<>();
        }
    }
    
    public List<PurchaseOrder> getSentPOsByWarehouse(int warehouseId) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(GET_SENT_PO_MANAGER_SQL)) {
            
            return executePOQuery(pstmt);
        } catch (SQLException e) {
            e.printStackTrace(); return new ArrayList<>();
        }
    }
    
    public boolean receiveGoodsAndUpdateStock(int poId, int warehouseId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); 
            
            try (PreparedStatement psItems = conn.prepareStatement(GET_PO_ITEMS_SQL)) {
                psItems.setInt(1, poId);
                try (ResultSet rs = psItems.executeQuery()) {
                    
                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        int qty = rs.getInt("qty");
                        
                        try (PreparedStatement psUpdate = conn.prepareStatement(UPDATE_INVENTORY_SQL)) {
                            psUpdate.setInt(1, warehouseId);
                            psUpdate.setInt(2, productId);
                            psUpdate.setInt(3, qty);
                            psUpdate.setInt(4, qty);
                            psUpdate.executeUpdate();
                        }
                    }
                }
            }
            
            try (PreparedStatement psStatus = conn.prepareStatement(UPDATE_PO_DELIVERED_SQL)) {
                psStatus.setInt(1, poId);
                psStatus.executeUpdate();
            }

            conn.commit(); 
            return true;
            
        } catch (SQLException e) {
            System.err.println("Goods Receipt Transaction FAILED for PO " + poId);
            e.printStackTrace();
            try { 
                if (conn != null) conn.rollback(); // 5. ROLLBACK on error
            } catch (SQLException rollbackEx) { 
                rollbackEx.printStackTrace(); 
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // Reset auto-commit
                DBUtil.close(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}