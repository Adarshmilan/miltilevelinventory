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
                <a href="InventoryServlet" class="nav-link active">
                    <span class="nav-icon">📦</span> Inventory
                </a>
            </c:if>
            
            <c:if test="${sessionScope.role eq 'Admin' || sessionScope.role eq 'Supplier' || sessionScope.role eq 'WarehouseManager'}">
                <a href="POServlet" class="nav-link">
                    <span class="nav-icon">🛒</span> Purchase Orders
                </a>
            </c:if>

            <c:if test="${sessionScope.role eq 'WarehouseManager'|| sessionScope.role eq 'Admin'}">
                 <a href="TransferServlet" class="nav-link">
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
            <c:if test="${sessionScope.role eq 'WarehouseManager'}">
                <c:if test="${param.message != null}">
                    <div class="alert-msg alert-success">
                        <span class="icon">✅</span> <c:out value="${param.message}"/>
                    </div>
                </c:if>

                <c:if test="${param.error != null}">
                    <div class="alert-msg alert-error">
                        <span class="icon">❌</span> Error: <c:out value="${param.error}"/>
                    </div>
                </c:if>
                <div class="inventory-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Product ID</th>
                                <th>Name</th>
                                <th>Reorder Level</th>
                                <th>Current Quantity</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty requestScope.inventoryList}">
                                    <c:forEach var="product" items="${requestScope.inventoryList}">

                                        <tr class="<c:if test="${product.lowStock}">low-stock</c:if>">

                                            <td><c:out value="${product.productId}"/></td>
                                            <td><c:out value="${product.name}"/></td>
                                            <td><c:out value="${product.reorderLevel}"/></td>
                                            <td><c:out value="${product.currentQuantity}"/></td>

                                            <td>
                                                <c:choose>
                                                    <c:when test="${product.lowStock}">
                                                        <span class="status-badge status-low">Low Stock</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge status-instock">In Stock</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td>
                                                <c:if test="${product.lowStock}">
                                                    <form action="POServlet" method="post" class="action-form">
                                                        <input type="hidden" name="action" value="createRequest"/>
                                                        <input type="hidden" name="productId" value="${product.productId}"/>
                                                        <input type="number" name="requestedQuantity" value="100" min="1" class="qty-input" title="Quantity to Order"> 
                                                        <input type="hidden" name="warehouseId" value="${requestScope.warehouseId}"/>
                                                        <button type="submit" class="create-po-btn" style="background-color: var(--alert-low-stock);">Create PO</button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${!product.lowStock}">
                                                    <button class="action-button-edit" style="background-color: var(--secondary-color);">All good</button>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr><td colspan="6">No inventory records found for this warehouse.</td></tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </c:if>
            
            <c:if test="${sessionScope.role eq 'Admin'}">
                <c:if test="${param.message != null}">
                    <div class="alert-msg alert-success">
                        <span class="icon">✅</span> <c:out value="${param.message}"/>
                    </div>
                </c:if>

                <c:if test="${param.error != null}">
                    <div class="alert-msg alert-error">
                        <span class="icon">❌</span> Error: <c:out value="${param.error}"/>
                    </div>
                </c:if>
                <div class="inventory-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Product ID</th>
                                <th>Name</th>
                                <th>Reorder Level</th>
                                <th>Current Quantity</th>
                                <th>Status</th>
                                <th>Warehouse id</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty requestScope.inventoryList}">
                                    <c:forEach var="product" items="${requestScope.inventoryList}">

                                        <tr class="<c:if test="${product.lowStock}">low-stock</c:if>">

                                            <td><c:out value="${product.productId}"/></td>
                                            <td><c:out value="${product.name}"/></td>
                                            <td><c:out value="${product.reorderLevel}"/></td>
                                            <td><c:out value="${product.currentQuantity}"/></td>

                                            <td>
                                                <c:choose>
                                                    <c:when test="${product.lowStock}">
                                                        <span class="status-badge status-low">Low Stock</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge status-instock">In Stock</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            
                                            <td><c:out value="${product.warehouseid}"  /></td>

                                            <td>
                                                <c:if test="${product.lowStock}">
                                                    <form action="POServlet" method="post" class="action-form">
                                                        <input type="hidden" name="action" value="createRequest"/>
                                                        <input type="hidden" name="productId" value="${product.productId}"/>
                                                        <input type="number" name="requestedQuantity" value="100" min="1" class="qty-input" title="Quantity to Order"> 
                                                        <input type="hidden" name="warehouseId" value="${requestScope.warehouseId}"/>
                                                        <button type="submit" class="create-po-btn" style="background-color: var(--alert-low-stock);">Create PO</button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${!product.lowStock}">
                                                    <button class="action-button-edit" style="background-color: var(--secondary-color);">All good</button>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr><td colspan="6">No inventory records found for this warehouse.</td></tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </c:if>
            
        </div>
    </div>
</body>
</html>