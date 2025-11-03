<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>System Login</title>
    <style>
        /* Minimalist Reset & Global Styles */
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            background-color: #f7f7f7; /* Light Gray Background */
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        
        /* Main Login Card Container */
        .login-card {
            background-color: #ffffff;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); /* Subtle Shadow */
            width: 350px;
            text-align: center;
            align-items: center;
            justify-content: center;
        }

        /* Catchy Graphic Element (Subtle Bar) */
        .login-card::before {
            content: '';
            display: block;
            width: 100%;
            height: 4px;
            background: linear-gradient(to right, #444444, #888888); /* Gray gradient for subtle appeal */
            border-radius: 4px 4px 0 0;
            margin: -40px 0 30px -40px; /* Position at the top edge */
            width: calc(100% + 80px); /* Extend full width including padding */

        }
        
        /* Typography */
        h2 {
            font-size: 1.25rem;
            color: #222222;
            margin-bottom: 25px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        .comlogo{
            padding-left: 130px;
            height: 100px;
            width: 100px
        }
        .log-in-label {
            font-size: 0.8rem;
            color: #555;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            display: block;
            text-align: left;
            margin-bottom: 10px;
        }

        /* Form Inputs */
        input[type="email"],
        input[type="password"] {
            width: 100%;
            padding: 12px;
            margin-bottom: 15px;
            border: 1px solid #cccccc;
            border-radius: 4px;
            box-sizing: border-box;
            font-size: 0.9rem;
            color: #333;
        }
        
        /* Login Button */
        .login-btn {
            width: 100%;
            padding: 12px;
            background-color: #333333;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: bold;
            text-transform: uppercase;
            transition: background-color 0.2s;
            margin-top: 5px;
        }
        img{
            align-self: center;
            box-sizing: inherit;
            height: 100px;
            width: 100px;
        }
        .login-btn:hover {
            background-color: #555555;
        }

        /* Quick Login Section */
        .quick-login-section {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #eeeeee;
        }
        .quick-login-label {
            font-size: 0.75rem;
            color: #777;
            margin-bottom: 15px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .quick-login-buttons {
            display: flex;
            justify-content: space-between;
        }
        .quick-btn {
            background: #f0f0f0; /* Light Gray Button Background */
            color: #444;
            border: 1px solid #ccc;
            padding: 10px 5px;
            width: 22%;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.75rem;
            font-weight: bold;
            transition: background 0.2s, color 0.2s;
        }
        .quick-btn:hover {
            background: #cccccc;
            color: #000;
        }
        
        /* Error Message Styling */
        .error-message {
            color: #cc0000;
            font-size: 0.9rem;
            margin-bottom: 15px;
            font-weight: bold;
        }
    </style>
</head>
<body>

    <div class="login-card">
        <div class ="comlogo"> <img src="src/box.png" alt="companylog"></div>
        <h2>MULTI-LEVEL INVENTORY SYSTEM</h2>
        
        <form id="loginForm" action="LoginServlet" method="post">
            <label class="log-in-label" for="email">Log In</label>

            <c:if test="${requestScope.error != null}">
                <div class="error-message"><c:out value="${requestScope.error}"/></div>
            </c:if>

            <input type="email" id="email" name="email" placeholder="Email" required>
            <input type="password" id="password" name="password" placeholder="Password" required>
            
            <button type="submit" class="login-btn">Log In</button>
        </form>

        <div class="quick-login-section">
            <div class="quick-login-label">OR QUICK LOG IN:</div>
            <div class="quick-login-buttons">
                <button class="quick-btn" 
                        onclick="fillForm('adar@try.com', '123')">ADMIN</button>
                
                <button class="quick-btn" 
                        onclick="fillForm('prym@try.com', '123')">MANAGER1</button>
                <button class="quick-btn" 
                        onclick="fillForm('sunil@try', '123')">MANAGER2</button>
                <button class="quick-btn" 
                        onclick="fillForm('tilu@123', '123')">MANAGER3</button>
                
                <button class="quick-btn" 
                        onclick="fillForm('duka@try.com', '123')">SUPPLIER</button>
                
            </div>
        </div>
    </div>

    <script>
        function fillForm(email, password) {
            document.getElementById('email').value = email;
            document.getElementById('password').value = password;
            // Optionally submit the form immediately:
            // document.getElementById('loginForm').submit(); 
        }
    </script>
</body>
</html>