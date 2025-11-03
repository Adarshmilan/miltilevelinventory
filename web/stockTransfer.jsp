<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dashboard - Inventory MS</title>
    <link rel="stylesheet" href="allcss/inventorycss.css" >
</head>
<body>

    <c:if test="${sessionScope.user == null}">
        <c:redirect url="login.jsp"/>
    </c:if>

    <div class="header">
        <div class="header-left">
            <div class="header-logo">
                <img src="src/box.png" alt="Inventory MS Logo" style="height:30px;" /> <%-- LEFT LOGO SOURCE HERE --%>
            </div>
            <div class="header-title">Inventory MS</div>
        </div>
        <div class="header-right">
            <div class="user-info">
                <span class="user-name"><c:out value="${sessionScope.user.name}"/></span>
                <span class="user-role">(<c:out value="${sessionScope.role}"/>)</span>
            </div>
            <a href="LogoutServlet" class="logout-btn" title="Logout">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-log-out"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
            </a>
        </div>
    </div>

    <div class="main-container">
        <div class="sidebar">
            <h4>Navigation</h4>
            
            <a href="DashboardServlet" class="nav-link ">
                <span class="nav-icon">📊</span> Dashboard
            </a>
            
            <c:if test="${sessionScope.role eq 'WarehouseManager' || sessionScope.role eq 'Admin'}">
                <a href="InventoryServlet" class="nav-link">
                    <span class="nav-icon">📦</span> Inventory
                </a>
            </c:if>
            
            <c:if test="${sessionScope.role eq 'Admin' || sessionScope.role eq 'Supplier' || sessionScope.role eq 'WarehouseManager'}">
                <a href="POServlet" class="nav-link">
                    <span class="nav-icon">🛒</span> Purchase Orders
                </a>
            </c:if>

            <c:if test="${sessionScope.role eq 'WarehouseManager'|| sessionScope.role eq 'Admin'}">
                 <a href="TransferServlet" class="nav-link active">
                    <span class="nav-icon">🚚</span> Stock Transfers
                </a>
            </c:if>
            
            <c:if test="${sessionScope.role eq 'WarehouseManager'|| sessionScope.role eq 'Admin'}">
                 <a href="RetailerShipmentServlet" class="nav-link">
                    <span class="nav-icon">🚚</span> Retailer Shipment
                </a>
            </c:if>
            
             <c:if test="${sessionScope.role eq 'Admin'}">
                <a href="reports.jsp" class="nav-link">
                    <span class="nav-icon">📈</span> Reports
                </a>
            </c:if>
        </div>

        <div class="content">
            <h2>🚚 Internal Stock Transfer Management</h2>

            <div class="transfer-grid">
                <div class="transfer-form-container">
                    <h3>Request New Stock Transfer (Source WH: <c:out value="${sessionScope.warehouseId}"/>)</h3>

                    <form action="TransferServlet" method="post" id="transferForm">
                        <input type="hidden" name="action" value="request_transfer">

                        <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px;">
                            <div class="form-group">
                                <label for="productId">Product to Transfer</label>
                                <select id="productId" name="productId" required onchange="updateMaxQtyHint()">
                                    <option value="" data-max-qty="0">-- Select Product --</option>
                                    <c:forEach var="product" items="${requestScope.availableStock}">
                                        <option value="${product.productId}" data-max-qty="${product.currentQuantity}">
                                            <c:out value="${product.name}"/> (Avail: <c:out value="${product.currentQuantity}"/>)
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label for="whereWh">Giver Warehouse</label>
                                <select id="toWh" name="whereWh" required>
                                    <option value="">-- Select from which warehouse --</option>
                                    <c:forEach var="wh" items="${requestScope.allWarehouses}">
                                        <option value="${wh.warehouseId}"><c:out value="${wh.name}"/></option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="toWh">Destination Warehouse</label>
                                <select id="toWh" name="toWh" required>
                                    <option value="">-- Select Destination --</option>
                                    <c:forEach var="wh" items="${requestScope.allWarehouses}">
                                        <c:if test="${wh.warehouseId != sessionScope.warehouseId}">
                                            <option value="${wh.warehouseId}"><c:out value="${wh.name}"/></option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="quantity">Quantity (Max: <span id="maxQtyHint">0</span>)</label>
                                <input type="number" id="quantity" name="quantity" min="1" required>
                            </div>
                        </div>

                        <button type="submit" class="action-button approve-btn">Submit Transfer Request</button>
                    </form>
                </div>

                <div class="section-box">
                    
                    <h2>Pending and Active Transfers</h2>

                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Product / Qty</th>
                                <th>Source (From)</th>
                                <th>Destination (To)</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty requestScope.transfers}">
                                    <c:forEach var="t" items="${requestScope.transfers}">
                                        <tr>
                                            <td><c:out value="${t.transferId}"/></td>
                                            <td>
                                                <strong><c:out value="${t.productName}"/></strong><br>
                                                <span style="font-size: 0.85em; color: var(--secondary-color);">Qty: <c:out value="${t.quantity}"/></span>
                                            </td>
                                            <td><c:out value="${t.fromWhName}"/></td>
                                            <td><c:out value="${t.toWhName}"/></td>
                                            <td>
                                                <span class="status-badge ${t.status eq 'Requested' ? 'requested' : t.status eq 'In Transit' ? 'intransit' : 'received'}">
                                                    <c:out value="${t.status}"/>
                                                </span>
                                            </td>
                                            <td>
                                                
                                                    
                                                <c:set var="isFromManager" value="${t.fromWh == sessionScope.warehouseId}"/>
                                                <c:set var="isToManager" value="${t.toWh == sessionScope.warehouseId}"/>

                                                <%-- Action buttons are conditional based on status and user's WH ID --%>
                                                <c:choose>

                                                    <%-- 1. APPROVAL (Only Source WH Manager, status is Requested) --%>
                                                    <c:when test="${t.status eq 'Requested' and isFromManager}">
                                                        <form action="TransferServlet" method="post" class="action-form">
                                                            <input type="hidden" name="action" value="approve_transfer"/>
                                                            <input type="hidden" name="transferId" value="${t.transferId}"/>
                                                            <button type="submit" class="action-button approve-btn">Approve & Ship</button>
                                                        </form>
                                                    </c:when>

                                                    <%-- 2. RECEIPT (Only Destination WH Manager, status is In Transit) --%>
                                                    <c:when test="${t.status eq 'In Transit' and isToManager}">
                                                        <form action="TransferServlet" method="post" class="action-form">
                                                            <input type="hidden" name="action" value="receive_transfer"/>
                                                            <input type="hidden" name="transferId" value="${t.transferId}"/>
                                                            <button type="submit" class="action-button receive-btn">Mark Received</button>
                                                        </form>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span style="color: var(--secondary-color); font-size: 0.8em;">No Action Needed</span>
                                                    </c:otherwise>
                                                </c:choose>
                                                
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr><td colspan="6" style="text-align:center;">No pending or active transfers.</td></tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <script>
                // JavaScript to validate quantity based on selected product stock
                function updateMaxQtyHint() {
                    const select = document.getElementById('productId');
                    const qtyInput = document.getElementById('quantity');
                    const hint = document.getElementById('maxQtyHint');

                    const selectedOption = select.options[select.selectedIndex];
                    const maxQty = parseInt(selectedOption.getAttribute('data-max-qty'));

                    hint.textContent = isNaN(maxQty) ? '0' : maxQty;
                    qtyInput.max = isNaN(maxQty) ? 0 : maxQty;
                }

                // Initial call and form validation setup
                document.addEventListener('DOMContentLoaded', function() {
                    updateMaxQtyHint();
                    document.getElementById('productId').addEventListener('change', updateMaxQtyHint);

                    document.getElementById('transferForm').onsubmit = function() {
                        const qtyInput = document.getElementById('quantity');
                        const max = parseInt(qtyInput.max);
                        const current = parseInt(qtyInput.value);
                        const toWhSelect = document.getElementById('toWh');

                        if (qtyInput.value === '' || current <= 0) {
                            alert('Transfer Failed: Quantity must be a positive number.');
                            return false;
                        }
                        if (current > max) {
                            alert('Transfer Failed: Requested quantity (' + current + ') exceeds available stock (' + max + ').');
                            return false;
                        }
                        if (toWhSelect.value === "") {
                            alert('Transfer Failed: Please select a destination warehouse.');
                            return false;
                        }
                        return true;
                    };
                });
            </script>
        </div>
    </div>
</body>
</html>