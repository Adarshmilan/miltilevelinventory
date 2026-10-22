# 🏭 Multi-Level Inventory & Supply Chain Management System

A Java web application for managing stock across **multiple warehouses**, running the full **purchase-order lifecycle**, and moving stock between warehouses – with separate roles for Admins, Warehouse Managers and Suppliers.

> 📄 Full write-up: [`Project Report - SCM System.pdf`](Project%20Report%20-%20SCM%20System.pdf)

## ✨ Features

- **Role-based login** – Admin, Warehouse Manager, Supplier
- **Dashboard** with key stats per user/warehouse
- **Inventory** per warehouse with low-stock alerts (reorder level)
- **Purchase orders** – request → approve → ship → receive
- **Stock transfers** between warehouses
- **Retailer shipments**
- **Reports**
- Flash messages and the Post/Redirect/Get pattern to avoid duplicate form submits
- Multi-step stock changes run inside **database transactions** (commit / rollback)

## 🔄 Workflows

### Purchase order
```
Pending ──(Admin approves)──▶ Approved ──(Supplier ships)──▶ Sent ──(Manager receives)──▶ Delivered
                                                                         └─ inventory += qty (transaction)
```

### Stock transfer
```
Requested ──(source manager ships)──▶ In Transit ──(destination manager receives)──▶ Received
                └─ source −qty, destination +qty (one transaction)
```

### Roles
| Role | Can do |
|------|--------|
| Admin | Approve pending POs, system configuration |
| Warehouse Manager | Create POs, manage local stock, start transfers, confirm goods receipt |
| Supplier | See approved POs for their company, mark them Sent or Rejected |

## 🧱 Architecture (MVC)

```
src/java/
├── servlets/   # Controllers: Login, Logout, Dashboard, Inventory, PO, Transfer, RetailerShipment, Report
├── DAO/        # SQL + transactions: Dashboard, Inventory, PurchaseOrder, Transfer, User
├── model/      # Product, PurchaseOrder, RetailShipment, StockTransfer, User, Warehouse
└── util/DBUtil.java   # JDBC connection (reads DB_URL / DB_USER / DB_PASSWORD)
web/
├── login.jsp, dashboard_main.jsp, inventory.jsp, purchaseOrders.jsp,
│   stockTransfer.jsp, retailerShipments.jsp, reports.jsp
├── allcss/inventorycss.css
└── WEB-INF/web.xml
```

## 🛠️ Tech stack

- **Java 17**, Servlets, **JSP + JSTL 1.2**
- **MySQL / MariaDB** via MySQL Connector/J 8.2
- **Apache Tomcat 9**
- NetBeans / Ant project (`build.xml`)
- **Docker** multi-stage build

## 🚀 Running it

### With Docker

```bash
docker build -t miltilevelinventory .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/inventory" \
  -e DB_USER=root \
  -e DB_PASSWORD=yourpassword \
  miltilevelinventory
```

Open `http://localhost:8080/`.

### With NetBeans + Tomcat

1. Open the project in NetBeans.
2. Add MySQL Connector/J and JSTL 1.2 jars to the libraries.
3. Create a MySQL database called `inventory`.
4. Run on a Tomcat 9 server.

If no environment variables are set, the app connects to `jdbc:mysql://localhost:3306/inventory` as `root` with an empty password.
