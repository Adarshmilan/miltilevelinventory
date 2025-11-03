/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Product;
import model.RetailShipment;
import util.DBUtil;
/**
 *
 * @author ADARSH MILAN
 */
public class InventoryDAO {

    private static final String GET_INVENTORY_SQL =
        "SELECT p.product_id, p.name, p.category, p.reorder_level, i.quantity " +
        "FROM Products p JOIN Inventory i ON p.product_id = i.product_id " +
        "WHERE i.warehouse_id = ?";
    
    private static final String getallinventory = 
        "SELECT p.product_id, p.name, p.category, p.reorder_level, i.quantity, i.warehouse_id " +
        "FROM Products p JOIN Inventory i ON p.product_id = i.product_id ORDER BY i.warehouse_id;";
    
    private static final String GET_WAREHOUSE_ID_SQL =
        "SELECT warehouse_id FROM Warehouses WHERE manager_id = ?";
    
    // Goods Receipt: Final step to update inventory
    private static final String GET_PO_ITEMS_SQL = 
        "SELECT product_id, qty FROM PO_Items WHERE po_id = ?";
    private static final String UPDATE_INVENTORY_SQL = 
        "INSERT INTO Inventory (warehouse_id, product_id, quantity) VALUES (?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
    private static final String UPDATE_PO_DELIVERED_SQL =
        "UPDATE PurchaseOrders SET status = 'Delivered' WHERE po_id = ?";
    
    
    //yaha manager products ko sell karega retailers tk
    private static final String INSERT_SHIPMENT_SQL =
        "INSERT INTO RetailShipments (warehouse_id, product_id, quantity, retailer_reference, status) VALUES (?, ?, ?, ?, 'Shipped')";
    private static final String DECREMENT_INVENTORY_SQL =
        "UPDATE Inventory SET quantity = quantity - ? WHERE warehouse_id = ? AND product_id = ? AND quantity >= ?";
    private static final String GET_SHIPMENTS_SQL =
        "SELECT rs.shipment_id, rs.quantity, rs.ship_date, rs.retailer_reference, p.name AS product_name " +
        "FROM RetailShipments rs JOIN Products p ON rs.product_id = p.product_id WHERE rs.warehouse_id = ? ORDER BY rs.ship_date DESC";


    public int getWarehouseId(int userId) throws SQLException {
        Integer warehouseId = null;
        
        try (Connection co = DBUtil.getConnection();
             PreparedStatement ps = co.prepareStatement(GET_WAREHOUSE_ID_SQL)) {
            
            ps.setInt(1, userId);
            
            try (ResultSet r = ps.executeQuery()) {
                if (r.next()) {
                    warehouseId = r.getInt("warehouse_id");
                }
            }
        }
        return warehouseId; 
    }

    public List<Product> getInventoryByWarehouse(int warehouseId) {
        List<Product> inventoryList = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(GET_INVENTORY_SQL)) {
            
            pstmt.setInt(1, warehouseId); 

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setName(rs.getString("name"));
                    product.setCategory(rs.getString("category"));
                    product.setReorderLevel(rs.getInt("reorder_level"));
                    product.setCurrentQuantity(rs.getInt("quantity"));
                    
                    if (product.getCurrentQuantity() < product.getReorderLevel()) {
                        product.setLowStock(true);
                    } else {
                        product.setLowStock(false);
                    }
                    
                    inventoryList.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error retrieving inventory: " + e.getMessage());
            e.printStackTrace();
        }
        return inventoryList;
    }
    
    public List<Product> getallinventoryitems(){
        List<Product> inventoryList = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getallinventory)) {
            

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setName(rs.getString("name"));
                    product.setCategory(rs.getString("category"));
                    product.setReorderLevel(rs.getInt("reorder_level"));
                    product.setCurrentQuantity(rs.getInt("quantity"));
                    product.setWarehouseid(rs.getInt("warehouse_id"));
                    
                    if (product.getCurrentQuantity() < product.getReorderLevel()) {
                        product.setLowStock(true);
                    } else {
                        product.setLowStock(false);
                    }
                    
                    inventoryList.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error retrieving inventory: " + e.getMessage());
            e.printStackTrace();
        }
        return inventoryList;
    }
    
    public boolean receiveGoodsAndUpdateStock(int poId, int warehouseId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get PO items (product and quantity)
            try (PreparedStatement psItems = conn.prepareStatement(GET_PO_ITEMS_SQL)) {
                psItems.setInt(1, poId);
                try (ResultSet rs = psItems.executeQuery()) {
                    
                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        int qty = rs.getInt("qty");
                        
                        // 2. Update Inventory (use ON DUPLICATE KEY UPDATE)
                        try (PreparedStatement psUpdate = conn.prepareStatement(UPDATE_INVENTORY_SQL)) {
                            psUpdate.setInt(1, warehouseId);
                            psUpdate.setInt(2, productId);
                            psUpdate.setInt(3, qty); // INSERT value
                            psUpdate.setInt(4, qty); // UPDATE value
                            psUpdate.executeUpdate();
                        }
                    }
                }
            }
            
            // 3. Update PO status to Delivered
            try (PreparedStatement psStatus = conn.prepareStatement(UPDATE_PO_DELIVERED_SQL)) {
                psStatus.setInt(1, poId);
                psStatus.executeUpdate();
            }

            conn.commit(); 
            return true;
            
        } catch (SQLException e) {
            System.err.println("Goods Receipt Transaction FAILED for PO " + poId);
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                DBUtil.close(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public List<Product> getAvailableStockForShipment(int warehouseId) throws SQLException {
        return getInventoryByWarehouse(warehouseId);
    }
    
    public boolean issueStockToRetailer(int whId, int pId, int qty, String retailer) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); 

            
            try (PreparedStatement psUpdate = conn.prepareStatement(DECREMENT_INVENTORY_SQL)) {
                psUpdate.setInt(1, qty);          
                psUpdate.setInt(2, whId);
                psUpdate.setInt(3, pId);
                psUpdate.setInt(4, qty);           

                int rowsAffected = psUpdate.executeUpdate();
                if (rowsAffected == 0) {                    
                    conn.rollback();
                    return false; 
                }
            }

            
            try (PreparedStatement psInsert = conn.prepareStatement(INSERT_SHIPMENT_SQL)) {
                psInsert.setInt(1, whId);
                psInsert.setInt(2, pId);
                psInsert.setInt(3, qty);
                psInsert.setString(4, retailer);
                psInsert.executeUpdate();
            }

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            System.err.println("Stock Issuance Transaction FAILED: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                DBUtil.close(conn);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    public List<RetailShipment> getRetailShipmentsByWarehouse(int warehouseId) {
        List<RetailShipment> shipments = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(GET_SHIPMENTS_SQL)) {
            pstmt.setInt(1, warehouseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    RetailShipment rsmt = new RetailShipment();
                    rsmt.setShipmentId(rs.getInt("shipment_id"));
                    rsmt.setProductName(rs.getString("product_name"));
                    rsmt.setQuantity(rs.getInt("quantity"));
                    rsmt.setRetailerName(rs.getString("retailer_reference"));
                    rsmt.setShipDate(rs.getString("ship_date")); 
                    // Status is hardcoded to 'Shipped' in the table, can be retrieved if added later
                    rsmt.setStatus("Shipped"); 
                    shipments.add(rsmt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching retail shipments: " + e.getMessage());
        }
        return shipments;
    }
}
