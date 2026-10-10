`miltilevelinventory` is a Java web application built to run on Apache Tomcat. The application compiles Java servlet sources, leverages JavaServer Pages (JSP) with the JavaServer Pages Standard Tag Library (JSTL), and uses the MySQL Connector/J driver to connect to a MySQL database backend.

## Tech Stack

* **Language:** Java (JDK 17)
* **Web Server / Servlet Container:** Apache Tomcat 9.0
* **Libraries & Dependencies:**
  * Java Servlet API
  * JavaServer Pages (JSP) API
  * JSTL 1.2 (`javax.servlet/jstl`)
  * MySQL Connector/J 8.2.0 (`com.mysql/mysql-connector-j`)
* **Containerization:** Docker (Multi-stage build)

## Setup

The application is containerized using Docker, which compiles the Java source code, packages the `ROOT.war` file, and configures the Tomcat server environment.

### Prerequisites

* [Docker](https://docs.docker.com/get-docker/) installed on your machine.

### Build and Run

1. **Build the Docker image:**

   ```bash
   docker build -t miltilevelinventory .
   ```

2. **Run the container:**

   ```bash
   docker run -p 8080:8080 miltilevelinventory
   ```

The application will be accessible at `http://localhost:8080`.