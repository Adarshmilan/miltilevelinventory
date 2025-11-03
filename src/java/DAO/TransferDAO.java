
package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import model.StockTransfer;
import model.*;
import util.DBUtil;

/**
 *
 * @author ADARSH MILAN
 */
public class TransferDAO {
    private static final String GET_ALL_WAREHOUSES_SQL = 
        "SELECT warehouse_id, name FROM Warehouses ORDER BY warehouse_id";
    private static final String INSERT_TRANSFER_SQL =
        "INSERT INTO StockTransfers (from_wh, to_wh, product_id, qty, status) VALUES (?, ?, ?, ?, 'Requested')";
    private static final String GET_PENDING_TRANSFERS_SQL =
        "SELECT st.transfer_id, st.from_wh, st.to_wh, st.qty, st.status, st.product_id, " +
        "p.name AS product_name, w_from.name AS from_wh_name, w_to.name AS to_wh_name " +
        "FROM StockTransfers st JOIN Products p ON st.product_id = p.product_id " +
        "JOIN Warehouses w_from ON st.from_wh = w_from.warehouse_id " +
        "JOIN Warehouses w_to ON st.to_wh = w_to.warehouse_id " +
        "WHERE st.status = 'Requested' OR st.status = 'In Transit' ORDER BY st.transfer_id DESC";
    private static final String UPDATE_TRANSFER_STATUS_SQL =
        "UPDATE StockTransfers SET status = ? WHERE transfer_id = ?";
    
    // SQL to check stock before issuing transfer
    private static final String CHECK_STOCK_SQL = 
        "SELECT quantity FROM Inventory WHERE warehouse_id = ? AND product_id = ?";
    
    // SQL for the atomic stock adjustment: DECREMENT FROM SOURCE, INCREMENT TO DESTINATION
    private static final String DECREMENT_SOURCE_SQL =
        "UPDATE Inventory SET quantity = quantity - ? WHERE warehouse_id = ? AND product_id = ? AND quantity >= ?";
    private static final String INCREMENT_DESTINATION_SQL =
        "INSERT INTO Inventory (warehouse_id, product_id, quantity) VALUES (?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE quantity = quantity + ?";


    public List<Warehouse> getAllWarehouses() throws SQLException {
        List<Warehouse> warehouses = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(GET_ALL_WAREHOUSES_SQL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Warehouse w = new Warehouse();
                w.setWarehouseId(rs.getInt("warehouse_id"));
                w.setName(rs.getString("name"));
                warehouses.add(w);
            }
        }
        return warehouses;
    }
    
    
    public boolean createTransferRequest(StockTransfer transfer) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_TRANSFER_SQL)) {
            
            pstmt.setInt(1, transfer.getFromWh());
            pstmt.setInt(2, transfer.getToWh());
            pstmt.setInt(3, transfer.getProductId());
            pstmt.setInt(4, transfer.getQuantity());
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating transfer request: " + e.getMessage());
            throw e;
        }
    }

    public List<StockTransfer> getTransferRequests() throws SQLException {
        List<StockTransfer> transfers = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(GET_PENDING_TRANSFERS_SQL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                StockTransfer st = new StockTransfer();
                st.setTransferId(rs.getInt("transfer_id"));
                st.setFromWh(rs.getInt("from_wh"));
                st.setFromWhName(rs.getString("from_wh_name"));
                st.setToWh(rs.getInt("to_wh"));
                st.setToWhName(rs.getString("to_wh_name"));
                st.setProductId(rs.getInt("product_id"));
                st.setProductName(rs.getString("product_name"));
                st.setQuantity(rs.getInt("qty"));
                st.setStatus(rs.getString("status"));
                transfers.add(st);
            }
        }
        return transfers;
    }
    
    public boolean executeTransfer(int transferId, int fromWh, int toWh, int productId, int qty) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. DECREMENT SOURCE INVENTORY (Critical: Validates sufficient stock exists)
            try (PreparedStatement psDec = conn.prepareStatement(DECREMENT_SOURCE_SQL)) {
                psDec.setInt(1, qty); // Quantity to subtract
                psDec.setInt(2, fromWh);
                psDec.setInt(3, productId);
                psDec.setInt(4, qty); // WHERE quantity >= qty (Validation)
                
                if (psDec.executeUpdate() == 0) {
                    conn.rollback();
                    // Log failure: insufficient stock
                    throw new SQLException("Insufficient stock in source warehouse to execute transfer.");
                }
            }
            
            // 2. INCREMENT DESTINATION INVENTORY
            try (PreparedStatement psInc = conn.prepareStatement(INCREMENT_DESTINATION_SQL)) {
                psInc.setInt(1, toWh);
                psInc.setInt(2, productId);
                psInc.setInt(3, qty); // Value for INSERT
                psInc.setInt(4, qty); // Value for UPDATE
                psInc.executeUpdate();
            }

            // 3. Update Transfer Status to 'In Transit'
            try (PreparedStatement psStatus = conn.prepareStatement(UPDATE_TRANSFER_STATUS_SQL)) {
                psStatus.setString(1, "In Transit");
                psStatus.setInt(2, transferId);
                psStatus.executeUpdate();
            }

            conn.commit(); 
            return true;
            
        } catch (SQLException e) {
            System.err.println("Transfer Transaction FAILED: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                DBUtil.close(conn);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
     public boolean updateTransferStatus(int transferId, String newStatus) throws SQLException {
         try (Connection conn = DBUtil.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(UPDATE_TRANSFER_STATUS_SQL)) {
             pstmt.setString(1, newStatus);
             pstmt.setInt(2, transferId);
             return pstmt.executeUpdate() > 0;
         }
     }
}
