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
            
            <a href="DashboardServlet" class="nav-link active">
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
            <div class="content-header">
                <h1>Welcome, <c:out value="${sessionScope.user.name}"/></h1>
                <p>Here's what's happening with your inventory today.</p>
            </div>

            <div class="metric-grid">
                <div class="metric-card">
                    <div class="metric-card-top">
                        <h3><c:out value="${coreMetrics.totalProducts}"/></h3>
                        <span class="metric-card-icon">🛒</span>
                    </div>
                    <p>Total Products</p>
                    <p style="font-size: 0.8em; color: #999;">Across all categories</p>
                </div>
                
                <div class="metric-card">
                    <div class="metric-card-top">
                        <h3><c:out value="${coreMetrics.totalWarehouses}"/></h3>
                        <span class="metric-card-icon" style="background-color: #fce4ec; color: #e91e63;">🏢</span>
                    </div>
                    <p>Warehouses</p>
                    <p style="font-size: 0.8em; color: #999;">Active locations</p>
                </div>
                
                <div class="metric-card">
                    <div class="metric-card-top">
                        <h3><c:out value="${coreMetrics.pendingPO}"/></h3>
                        <span class="metric-card-icon" style="background-color: #fff3e0; color: #ff9800;">🔔</span>
                    </div>
                    <p>Pending Orders</p>
                    <p style="font-size: 0.8em; color: #999;">Awaiting confirmation</p>
                </div>
                
                <div class="metric-card">
                    <div class="metric-card-top">
                        <h3 style="color: var(--alert-low-stock);"><c:out value="${empty lowStockItems ? 0 : lowStockItems.size()}"/></h3>
                        <span class="metric-card-icon" style="background-color: #ffcdd2; color: var(--alert-low-stock);">⚠️</span>
                    </div>
                    <p>Low Stock Alerts</p>
                    <p style="font-size: 0.8em; color: #999;">Items need reordering</p>
                </div>
            </div>

            <div class="dashboard-sections-grid">
                <div class="section-box">
                    <h2>Low Stock Alerts</h2>
                    
                    <c:choose>
                        <c:when test="${not empty lowStockItems}">
                            <c:forEach var="product" items="${lowStockItems}">
                                <div class="alert-item">
                                    <div class="alert-item-header">
                                        <span><c:out value="${product.name }"/>(wrho:- <c:out value="${product.warehouseid}"/>)</span>
                                        <span class="low-stock-label"><c:out value="${product.currentQuantity}"/> left</span>
                                    </div>
                                    <div class="stock-bar-container">
                                        <c:set var="percent" value="${(product.currentQuantity / product.reorderLevel) * 100}"/>
                                        <div class="stock-bar" style="width: ${percent > 100 ? 100 : percent}%; background-color: ${percent < 25 ? '#ff4d4f' : '#faad14'};"></div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p style="color:green;">All stocks are healthy in your assigned warehouse.</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                
            </div>
        </div>
    </div>
</body>
</html>