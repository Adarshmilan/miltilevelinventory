
package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ADARSH MILAN
 */
public class LogoutServlet extends HttpServlet {



    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Get the current session, but don't create a new one if it doesn't exist (false)
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // 2. Invalidate the session. This destroys all session-scoped attributes (like the 'user' object).
            session.invalidate(); 
            System.out.println("User logged out successfully.");
        }
        
        // 3. Redirect the user back to the login page
        response.sendRedirect("login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    }


}
