

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * Servlet implementation class Home
 */
@WebServlet("/Home")
public class Home extends HttpServlet {
    private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    protected Connection conn = null;

    public void init() throws ServletException {

        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("database go here");
            conn = ds.getConnection();
        } catch (SQLException e) {
            log("SQLException:" + e.getMessage());
        } catch (Exception e) {
            log("Exception:" + e.getMessage());
        }
    }
      
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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(true);

        String login = (String)session .getAttribute("login");
        if (login == null || login != "OK") {
            response.sendRedirect(request.getContextPath() + "/Login.jsp");
        } else {
            ArrayList<String> profile = new ArrayList<String>();
            ArrayList<String> shift = new ArrayList<String>();
            int customer_id = 0;

            String role = (String)session.getAttribute("role");
            Integer user_id = (Integer)session.getAttribute("user_id");
            if (role.equals("admin")) {   // in case of admin role
                try {
                    String sql = "SELECT id, login_name, first_name, middle_name, last_name, address"
                               + " FROM users"
                               + " WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, user_id);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        profile.add("Administrator");    // role
                        profile.add(String.valueOf(rs.getInt(1)));    // users.id
                        profile.add(rs.getString(2));    // login_name
                        profile.add(rs.getString(3));    // first_name
                        profile.add(rs.getString(4));   // last_name
                        request.setAttribute("profile", profile);
                        request.getRequestDispatcher("/Home.jsp").forward(request, response);
                    } else {
                        request.getRequestDispatcher("/Login.jsp").forward(request, response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (role.equals("customer")) {   // in case of medical staff role
                try {
                    String sql = "SELECT"
                               + " s.id, u.login_name, u.first_name, u.last_name,"
                               + " u.address, s.qualification, DATE_FORMAT(s.certification_expirations, '%m/%d/%Y'),"
                               + " s.cell_phone_number, s.email_address, s.personal_details"
                               + " FROM users u"
                               + " INNER JOIN"
                               + " (SELECT id, user_id,"
                               + " cell_phone_number, email_address, personal_details,"
                               + " FROM customer) s"
                               + " ON u.id = s.user_id"
                               + " WHERE u.id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, user_id);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        profile.add("customer");    // role
                        staff_id = rs.getInt(1);
                        profile.add(String.valueOf(customer_id));    
                        profile.add(rs.getString(2));    // login_name
                        profile.add(rs.getString(3));    // first_name
                        profile.add(rs.getString(4));   // middle_name
                        profile.add(rs.getString(5));   // last_name
                        profile.add(rs.getString(6));   // address
                        profile.add(rs.getString(9));   // cell_phone_number
                        profile.add(rs.getString(10));   // email_address
                            pstmt.clearParameters();
                            sql = "SELECT id FROM customer WHERE customer_id = ";
                            pstmt = conn.prepareStatement(sql);
                            rs = pstmt.executeQuery();
                        }
                    } else {
                        request.getRequestDispatcher("/Login.jsp").forward(request, response);
                    request.getRequestDispatcher("/Home.jsp").forward(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        
            }
        }
    }
}
