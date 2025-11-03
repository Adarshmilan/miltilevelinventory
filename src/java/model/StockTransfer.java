
package model;

import java.time.LocalDateTime;

/**
 *
 * @author ADARSH MILAN
 */
public class StockTransfer {
    private int transferId;
    private int fromWh;
    private String fromWhName;
    private int toWh;
    private String toWhName;
    private int productId;
    private String productName;
    private int quantity;
    private String status; 
    private LocalDateTime createdDate;

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getFromWh() {
        return fromWh;
    }

    public void setFromWh(int fromWh) {
        this.fromWh = fromWh;
    }

    public String getFromWhName() {
        return fromWhName;
    }

    public void setFromWhName(String fromWhName) {
        this.fromWhName = fromWhName;
    }

    public int getToWh() {
        return toWh;
    }

    public void setToWh(int toWh) {
        this.toWh = toWh;
    }

    public String getToWhName() {
        return toWhName;
    }

    public void setToWhName(String toWhName) {
        this.toWhName = toWhName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    
}
