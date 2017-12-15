import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    /**
     * @see HttpServlet#HttpServlet()
     */
    protected Connection conn = null;

    /**
     *
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("THIS IS WHERE THE DATABASE PATH GO");
            conn = ds.getConnection();
        } catch (SQLException e) {
            log("SQLException:" + e.getMessage());
        } catch (NamingException e) {
            log("Exception:" + e.getMessage());
        }
    }
      
    @Override
    public void destroy() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            log("SQLException:" + e.getMessage());
        }
    }
    
    /**
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");

        HttpSession session = request.getSession(true);
    
        boolean check = authUser(user, pass);
        if (check) {
            /* authentication successful */
            session.setAttribute("login", "OK");
            session.setAttribute("status", "Auth");

            int role = getRole(user, pass);
            switch (role) {
                case 0:
                    session.setAttribute("role", "admin");
                    break;
                case 1:
                    session.setAttribute("role", "customer");
                    break;
                default:
                    break;
            }

            int user_id = getUserId(user, pass);
            session.setAttribute("user_id", user_id);
            response.sendRedirect(request.getContextPath() + "/Home");
        } else {
            /* when authentication is failed, redirect to the login page. */
            session.setAttribute("status", "Not Auth");
            response.sendRedirect(request.getContextPath() + "/Login.jsp");
        }
    }
    
    protected boolean authUser(String user, String pass) {
        if (user == null || user.length() == 0 || pass == null || pass.length() == 0) {
            return false;
        }
        try {
            String sql = "SELECT id FROM users WHERE login_name = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, user);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            log("SQLException:" + e.getMessage());
            return false;
        }
    }

    protected int getRole(String user, String pass) {    
        try {
            String sql = "SELECT role FROM users WHERE login_name = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, user);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            log("SQLException:" + e.getMessage());
            return 0;
        }
    }

    protected int getUserId(String user, String pass) {    
        try {
            String sql = "SELECT id FROM users WHERE login_name = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, user);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            log("SQLException:" + e.getMessage());
            return 0;
        }
    }