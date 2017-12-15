import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Servlet implementation class customer
 */
@WebServlet("/customer")
public class customer extends HttpServlet {
    private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    protected Connection conn = null;
    protected HashMap<String,String> validationMap = new HashMap<String,String>();

    public void init() throws ServletException {

        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("database gose here");
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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/customer.jsp");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String firstname = request.getParameter("firstname");
        String middlename = request.getParameter("middlename");
        String lastname = request.getParameter("lastname");
        String ssn = request.getParameter("ssn");
        String date_of_birth = request.getParameter("date_of_birth");
        String username = request.getParameter("username");
        String starting_weight = request.getParameter("starting_weight");
        String goal_weight = request.getParameter("goal_weight");
        String address = request.getParameter("address");
        String password = request.getParameter("password");


        int user_id = 0;

        validationMap.clear();

        boolean checkName = validateName(firstname, lastname);
        boolean checkSSN = validateSSN(ssn);

        if (checkName && checkSSN) {
            try {
                /* Insert the form data to users table. */
                String sql = "INSERT INTO"
                           + " users (created_at, updated_at, username, password, role, first_name, middle_name, last_name, social_security_number, address)"
                           + " VALUES (STR_TO_DATE(?, '%m/%d/%Y'), CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, date);
                pstmt.setString(2, firstname + "." + lastname);
                pstmt.setString(3, password);
                pstmt.setInt(4, 2);
                pstmt.setString(5, firstname);
                pstmt.setString(6, middlename);
                pstmt.setString(7, lastname);
                pstmt.setString(8, ssn);
                pstmt.setString(9, address);
                pstmt.executeUpdate();
                
                pstmt.clearParameters();

                /* Get users.id of the inserted record avobe. */
                sql = "SELECT id FROM users"
                    + " WHERE first_name = ? AND last_name = ? AND social_security_number = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, firstname);
                pstmt.setString(2, lastname);
                pstmt.setString(3, ssn);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    user_id = rs.getInt(1);
                } else {
                    validationMap.put("registration", "failed");
                    request.setAttribute("validationMap", validationMap);
                    request.getRequestDispatcher("/customer.jsp").forward(request, response);
                }

                pstmt.clearParameters();

                /* Insert the form data, user_id into patients table. */
                sql = "INSERT INTO"
                    + " customer (user_id, created_at, updated_at, staring_weight, goal_weight)"
                    + " VALUES (?, ?, STR_TO_DATE(?, '%m/%d/%Y'), CURRENT_TIMESTAMP, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, user_id);
                pstmt.setInt(2, doctor_id);
                pstmt.setString(3, date);
                pstmt.setString(4, staring_weight);
                pstmt.setString(5, goal_weight);
                pstmt.executeUpdate();

                pstmt.clearParameters();

                /* Get the registration information from inserted records above, and forward it to customer.jsp. */
                sql = "SELECT"
                    + " p.id, u1.login_name, u1.password, u1.first_name, u1.middle_name, u1.last_name,"
                    + " DATE_FORMAT(u1.created_at, '%m/%d/%Y'), d2.first_name, d2.last_name, p.type, u1.address"
                    + " FROM users u1"
                    + " INNER JOIN"
                    + " (SELECT id, user_id, staring_weight, goal_weight FROM patients) p"
                    + " ON u1.id = p.user_id"
                    + " INNER JOIN"
                    + " (SELECT u2.first_name, u2.last_name, d1.id FROM users u2"
                    + " INNER JOIN"
                    + " WHERE u1.first_name = ? AND u1.last_name = ? AND u1.social_security_number = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, firstname);
                pstmt.setString(2, lastname);
                pstmt.setString(3, ssn);
                rs = pstmt.executeQuery();

                ArrayList<String> list = new ArrayList<String>();

                if (rs.next()) {
                    list.add(String.valueOf(rs.getInt(1)));    // customer_id
                    list.add(rs.getString(2));    // username
                    list.add(rs.getString(3));    // password
                    list.add(rs.getString(4));    // first_name
                    list.add(rs.getString(5));   // middle_name
                    list.add(rs.getString(6));   // last_name
                    list.add(rs.getString(7));   // DATE_FORMAT(created_at, '%m/%d/%Y')
                    list.add(rs.getString(8));   // staring_weight
                    list.add(rs.getString(9));   // goal_weight
                    list.add(rs.getString(10));   // address
                    validationMap.put("registration", "successful");
                    request.setAttribute("validationMap", validationMap);
                    request.setAttribute("list", list);
                    request.getRequestDispatcher("/customerResult.jsp").forward(request, response);
                } else {
                    validationMap.put("registration", "failed");
                    request.setAttribute("validationMap", validationMap);
                    request.getRequestDispatcher("/customer.jsp").forward(request, response);
                }

            } catch (Exception e) {
                validationMap.put("registration", "failed");
                request.setAttribute("validationMap", validationMap);
                e.printStackTrace();
            }

        } else {
            validationMap.put("registration", "failed");
            request.setAttribute("validationMap", validationMap);
            request.getRequestDispatcher("/customer.jsp").forward(request, response);
        }

    }

    protected boolean validateName(String firstname, String lastname) {
        try {
            String sql = "SELECT id FROM users WHERE first_name = ? AND last_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, firstname);
            pstmt.setString(2, lastname);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                validationMap.put("name", "in use");
                return false;
            } else {
                validationMap.put("name", "OK");
                return true;
            }
        } catch (SQLException e) {
               validationMap.put("name", "in use");
            log("SQLException:" + e.getMessage());
               return false;
        }
    }

    protected boolean validateSSN(String ssn) {
        try {
            String sql = "SELECT id FROM users WHERE social_security_number = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, ssn);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                validationMap.put("ssn", "in use");
                return false;
            } else {
                validationMap.put("ssn", "OK");
                return true;
            }
        } catch (SQLException e) {
               validationMap.put("ssn", "in use");
            log("SQLException:" + e.getMessage());
               return false;
        }
    }

}
