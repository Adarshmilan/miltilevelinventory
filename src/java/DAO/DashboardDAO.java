/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import util.DBUtil;
import java.sql.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import model.Product;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ADARSH MILAN
 */

public class DashboardDAO {

    private static final String COUNT_PRODUCTS_SQL = "SELECT COUNT(product_id) FROM Products";
    private static final String COUNT_WAREHOUSES_SQL = "SELECT COUNT(warehouse_id) FROM Warehouses";
    private static final String COUNT_PENDING_PO_SQL = "SELECT COUNT(po_id) FROM PurchaseOrders WHERE status = 'Pending'";
    private static final String LOW_STOCK_SQL = 
        "SELECT p.name, i.quantity, p.reorder_level " +
        "FROM Inventory i JOIN Products p ON i.product_id = p.product_id " +
        "WHERE i.quantity < p.reorder_level and i.warehouse_id = ?;";
    
    private static final String LOW_STOCK_SQL_foradmin = "SELECT p.name, i.quantity, p.reorder_level, i.warehouse_id FROM Inventory i JOIN Products p ON i.product_id = p.product_id WHERE i.quantity < p.reorder_level ORDER by i.warehouse_id;";

    public Map<String, Integer> getCoreMetrics() throws SQLException {
        Map<String, Integer> metrics = new HashMap<>();
        
        try (Connection conn = DBUtil.getConnection();
            Statement stmt = conn.createStatement()) {
            
            try (ResultSet rs = stmt.executeQuery(COUNT_PRODUCTS_SQL)) {
                if (rs.next()) metrics.put("totalProducts", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery(COUNT_WAREHOUSES_SQL)) {
                if (rs.next()) metrics.put("totalWarehouses", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery(COUNT_PENDING_PO_SQL)) {
                if (rs.next()) metrics.put("pendingPO", rs.getInt(1));
            }
        }catch(SQLException e){
            
        }
        return metrics;
    }
    
    public int getPendingPOsCount() throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(COUNT_PENDING_PO_SQL)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return 0;
            }
        }
    }

    public List<Product> getLowStockAlerts(int whid) throws SQLException {
        List<Product> alerts = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(LOW_STOCK_SQL)) {
            pstmt.setInt(1,whid);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setName(rs.getString("name"));
                    p.setCurrentQuantity(rs.getInt("quantity"));
                    p.setReorderLevel(rs.getInt("reorder_level"));
                    alerts.add(p);
                }
            }
        }
        return alerts;
    }

    public List<Product> getLowStockAlertsforadmin() throws SQLException {
        List<Product> alerts = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(LOW_STOCK_SQL_foradmin)) {
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setName(rs.getString("name"));
                    p.setCurrentQuantity(rs.getInt("quantity"));
                    p.setReorderLevel(rs.getInt("reorder_level"));
                    p.setWarehouseid(rs.getInt("warehouse_id"));
                    alerts.add(p);
                }
            }
        }
        return alerts;
    }
}
