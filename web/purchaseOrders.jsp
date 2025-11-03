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
                <a href="POServlet" class="nav-link active">
                    <span class="nav-icon">🛒</span> Purchase Orders
                </a>
            </c:if>

            <c:if test="${sessionScope.role eq 'WarehouseManager' || sessionScope.role eq 'Admin'}">
                 <a href="TransferServlet" class="nav-link">
                    <span class="nav-icon">🚚</span> Stock Transfers
                </a>
            </c:if>
            
            <c:if test="${sessionScope.role eq 'WarehouseManager' || sessionScope.role eq 'Admin'}">
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
            <h2 style="margin-bottom: 25px;">
                <c:choose>
                    <c:when test="${currentRole eq 'Admin'}">Admin: Pending POs for Review</c:when>
                    <c:when test="${currentRole eq 'Supplier'}">Supplier: Approved Orders Awaiting Confirmation</c:when>
                    <c:when test="${currentRole eq 'WarehouseManager'}">Manager: POs in Transit (Goods Receipt)</c:when>
                    <c:otherwise>Purchase Orders Overview</c:otherwise>
                </c:choose>
            </h2>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>PO ID</th>
                        <th>Product / Qty</th>
                        <th>Warehouse (WH ID)</th>
                        <th>Date Created</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty requestScope.poList}">
                            <c:forEach var="po" items="${requestScope.poList}">

                                <tr>
                                    <td><c:out value="${po.poId}"/></td>
                                    <td>                                        
                                        <strong><c:out value="${po.productName}"/></strong><br>                                        
                                        <span style="font-size: 0.85em; color: var(--secondary-color);">Qty: <c:out value="${po.requestedQuantity}"/></span>
                                    </td>
                                    <td>
                                        <!-- Warehouse Name (warehouse_name) -->
                                        <c:out value="${po.warehouseName}"/><br>
                                        <!-- Warehouse ID (warehouse_id) -->
                                        <span style="font-size: 0.85em; color: var(--secondary-color);">WH ID: <c:out value="${po.warehouseId}"/></span>
                                    </td>
                                    <td><c:out value="${po.createdDate}"/></td>

                                    <td>
                                        <c:set var="statusClass" value="${po.status == 'Pending' ? 'pending' : 
                                                                          po.status == 'Approved' ? 'approved' : 
                                                                          po.status == 'Sent' ? 'sent' : 
                                                                          po.status == 'Delivered' ? 'delivered' : 'rejected'}" />
                                        <span class="status-badge ${statusClass}">
                                            <c:out value="${po.status}"/>
                                        </span>
                                    </td>

                                    <td>
                                        <c:if test="${currentRole eq 'Admin'}">
                                            <form action="POServlet" method="post" class="action-form">
                                                <input type="hidden" name="action" value="approve"/>
                                                <input type="hidden" name="poId" value="${po.poId}"/>
                                                <button type="submit" class="action-button approve-btn">Approve</button>
                                            </form>
                                            <form action="POServlet" method="post" class="action-form">
                                                <input type="hidden" name="action" value="reject"/>
                                                <input type="hidden" name="poId" value="${po.poId}"/>
                                                <button type="submit" class="action-button reject-btn">Reject</button>
                                            </form>
                                        </c:if>

                                        <c:if test="${currentRole eq 'Supplier'}">
                                            <form action="POServlet" method="post" class="action-form">
                                                <input type="hidden" name="action" value="confirm_delivery"/>
                                                <input type="hidden" name="poId" value="${po.poId}"/>
                                                <button type="submit" class="action-button receive-btn">Confirm Shipping</button>
                                            </form>
                                            <form action="POServlet" method="post" class="action-form">
                                                <input type="hidden" name="action" value="supplier_reject"/>
                                                <input type="hidden" name="poId" value="${po.poId}"/>
                                                <button type="submit" class="action-button reject-btn">Reject PO</button>
                                            </form>
                                        </c:if>
                                        
                                        <c:if test="${currentRole eq 'WarehouseManager'}">
                                            <c:if test="${po.status eq 'Sent'}">
                                                <form action="POServlet" method="post" class="action-form">
                                                    <input type="hidden" name="action" value="receive_goods"/>
                                                    <input type="hidden" name="poId" value="${po.poId}"/>
                                                    <input type="hidden" name="warehouseid" value="${po.warehouseId}"/>
                                                    <button type="submit" class="action-button receive-btn">Receive Goods</button>
                                                </form>
                                            </c:if>
                                            <c:if test="${po.status eq 'Delivered'}">
                                                <span style="color: var(--alert-received); font-weight: 500;">Finalized</span>
                                            </c:if>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>

                        <c:otherwise>
                            <tr><td colspan="6" style="text-align: center;">No purchase orders requiring action for this status.</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>