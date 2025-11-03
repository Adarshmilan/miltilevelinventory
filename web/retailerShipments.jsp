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
                <a href="InventoryServlet" class="  nav-link">
                    <span class="nav-icon">📦</span> Inventory
                </a>
            </c:if>
            
            <c:if test="${sessionScope.role eq 'Admin' || sessionScope.role eq 'Supplier' || sessionScope.role eq 'WarehouseManager'}">
                <a href="POServlet" class="nav-link ">
                    <span class="nav-icon">🛒</span> Purchase Orders
                </a>
            </c:if>

            <c:if test="${sessionScope.role eq 'WarehouseManager' || sessionScope.role eq 'Admin'}">
                 <a href="TransferServlet" class="nav-link">
                    <span class="nav-icon">🚚</span> Stock Transfers
                </a>
            </c:if>
            
            <c:if test="${sessionScope.role eq 'WarehouseManager' || sessionScope.role eq 'Admin'}">
                 <a href="RetailerShipmentServlet" class="active nav-link">
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
            <h2>🚚 Retailer Shipment & Issuance</h2>

            <div class="shipment-form-container">
                <form action="RetailerShipmentServlet" method="post" id="issuanceForm">
                    <input type="hidden" name="action" value="issue_stock">

                    <div class="form-group">
                        <label for="productId">Select Product (Max Stock Available)</label>
                        <select id="productId" name="productId" required onchange="updateMaxQuantity()">
                            <option value="" data-max-qty="0">-- Select Product --</option>
                            <c:forEach var="product" items="${requestScope.availableStock}">
                                <option value="${product.productId}" data-max-qty="${product.currentQuantity}">
                                    <c:out value="${product.name}"/> (Current: <c:out value="${product.currentQuantity}"/>)
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="quantity">Quantity to Ship</label>
                        <input type="number" id="quantity" name="quantity" min="1" required>
                        <div class="max-qty-hint">Max available: <span id="currentStockHint">0</span></div>
                    </div>

                    <div class="form-group">
                        <label for="retailerName">Retailer Name / Reference</label>
                        <input type="text" id="retailerName" name="retailerName" required>
                    </div>

                    <div class="form-group">
                        <label>Warehouse ID</label>
                        <p style="font-weight: bold; color: var(--text-dark);"><c:out value="${sessionScope.warehouseId}"/></p>
                        <p style="font-size: 0.8em; color: var(--secondary-color);">Issuing from your assigned warehouse.</p>
                    </div>

                    <button type="submit" class="action-button approve-btn" style="width: 100%;">Issue Stock & Record Shipment</button>
                </form>
            </div>

            <h2>Outbound Shipments Recorded</h2>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>Shipment ID</th>
                        <th>Product</th>
                        <th>Quantity</th>
                        <th>Retailer</th>
                        <th>Status</th>
                        <th>Ship Date</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty requestScope.shipmentsList}">
                            <c:forEach var="shipment" items="${requestScope.shipmentsList}">
                                <tr>
                                    <td><c:out value="${shipment.shipmentId}"/></td>
                                    <td><c:out value="${shipment.productName}"/></td>
                                    <td><c:out value="${shipment.quantity}"/></td>
                                    <td><c:out value="${shipment.retailerName}"/></td>
                                    <td><span class="status-badge status-confirmed"><c:out value="${shipment.status}"/></span></td>
                                    <td><c:out value="${shipment.shipDate}"/></td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="6" style="text-align:center;">No retailer shipments recorded yet.</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>

            <script>
                // JavaScript for maximum quantity validation
                function updateMaxQuantity() {
                    const select = document.getElementById('productId');
                    const qtyInput = document.getElementById('quantity');
                    const hint = document.getElementById('currentStockHint');

                    // Get the selected option's max quantity data attribute
                    const selectedOption = select.options[select.selectedIndex];
                    const maxQty = parseInt(selectedOption.getAttribute('data-max-qty'));

                    hint.textContent = isNaN(maxQty) ? '0' : maxQty;
                    qtyInput.max = isNaN(maxQty) ? 0 : maxQty; // Set max attribute for native validation
                    qtyInput.value = ''; // Clear previous value
                }

                // Initial call to set the hint if the page reloads with a selected item
                updateMaxQuantity();

                // Prevent submission if quantity exceeds max available
                document.getElementById('issuanceForm').onsubmit = function() {
                    const qtyInput = document.getElementById('quantity');
                    const max = parseInt(qtyInput.max);
                    const current = parseInt(qtyInput.value);

                    if (current > max) {
                        alert('Cannot ship ' + current + '. Available stock is only ' + max + '.');
                        return false;
                    }
                    return true;
                };
            </script>
        </div>
         
    </div>
</body>
</html>