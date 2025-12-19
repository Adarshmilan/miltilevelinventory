# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM tomcat:9.0-jdk17 AS build

WORKDIR /app

# Copy all project files to the container
COPY . .

# 1. Setup Libraries
# Create the lib directory in case it's missing
RUN mkdir -p web/WEB-INF/lib

# Download MySQL Connector/J (JDBC Driver)
# We download this because the local project references it from a user's Downloads folder,
# which Docker cannot access.
ADD https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.2.0/mysql-connector-j-8.2.0.jar web/WEB-INF/lib/mysql-connector.jar

# 2. Compile Java Source Code
# We manually compile to avoid issues with NetBeans-specific build.xml files that contain absolute paths.
# -d build/classes: Output compiled .class files here
# -cp ...: Classpath includes Tomcat's Servlet API and our project libs
RUN mkdir -p build/classes && \
    javac -d build/classes \
    -cp "/usr/local/tomcat/lib/servlet-api.jar:/usr/local/tomcat/lib/jsp-api.jar:web/WEB-INF/lib/*" \
    $(find src/java -name "*.java")

# 3. Package into a WAR file
# We combine the 'web' folder content with our compiled classes
RUN mkdir -p dist && \
    jar -cvf dist/ROOT.war -C web . -C build/classes WEB-INF/classes

# ==========================================
# Stage 2: Run the Application
# ==========================================
FROM tomcat:9.0-jdk17-openjdk-slim

# Remove default Tomcat applications (docs, examples, etc.) to keep it clean
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file we built in Stage 1 to the Tomcat webapps directory
COPY --from=build /app/dist/ROOT.war /usr/local/tomcat/webapps/ROOT.war

# Expose the default Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
