import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Servlet implementation class SearchUser
 */
@WebServlet("/SearchUser")
public class SearchUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    protected Connection conn = null;
    protected HashMap<String,String> validationMap = new HashMap<String,String>();

    public void init() throws ServletException {

        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("database goes here");
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
        response.sendRedirect(request.getContextPath() + "/Search.jsp");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String firstname = request.getParameter("firstname");
        String lastname = request.getParameter("lastname");

        validationMap.clear();

        boolean checkFirstname = validateFirstname(firstname);
        boolean checkLastname = validateLastname(lastname);

        if (checkFirstname && checkLastname) {
            ArrayList<String> profile = new ArrayList<String>();
            ArrayList<String> shift = new ArrayList<String>();
            int role = getRole(firstname, lastname);
            if (role == 0) {   // in case of admin role
                try {
                    String sql = "SELECT id, login_name, first_name, middle_name, last_name, address"
                               + " FROM users"
                               + " WHERE first_name = ? AND last_name = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, firstname);
                    pstmt.setString(2, lastname);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        profile.add("Administrator");    // role
                        profile.add(String.valueOf(rs.getInt(1)));    // users.id
                        profile.add(rs.getString(2));    // login_name
                        profile.add(rs.getString(3));    // first_name
                        profile.add(rs.getString(4));   // middle_name
                        profile.add(rs.getString(5));   // last_name
                        profile.add(rs.getString(6));   // address
                        validationMap.put("user", "found");
                        request.setAttribute("validationMap", validationMap);
                        request.setAttribute("profile", profile);
                        request.setAttribute("shift", shift);
                        request.setAttribute("role", role);
                        request.getRequestDispatcher("/SearchUserResult.jsp").forward(request, response);
                    } else {
                        validationMap.put("user", "not found");
                        request.setAttribute("validationMap", validationMap);
                        request.getRequestDispatcher("/Search.jsp").forward(request, response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
           
                }
            } else if (role == 2) {   // in case of patient role
                try {
                    String sql = "SELECT"
                               + " p.id, u1.login_name, u1.first_name, u1.middle_name, u1.last_name,"
                               + " u1.address, DATE_FORMAT(u1.created_at, '%m/%d/%Y'),"
                               + " d2.first_name, d2.last_name, starting_weight, goal_weight"
                               + " FROM users u1"
                               + " INNER JOIN"
                               + " (SELECT id, user_id, type, starting_weight, goal_weight FROM customer) p"
                               + " ON u1.id = p.user_id"
                               + " INNER JOIN"
                               + " (SELECT u2.first_name, u2.last_name, d1.id FROM users u2"
                               + " WHERE u1.first_name = ? AND u1.last_name = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, firstname);
                    pstmt.setString(2, lastname);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        profile.add("customer");    // role
                        profile.add(String.valueOf(rs.getInt(1)));    // customer_id
                        profile.add(rs.getString(2));    // login_name
                        profile.add(rs.getString(3));    // first_name
                        profile.add(rs.getString(4));   // middle_name
                        profile.add(rs.getString(5));   // last_name
                        profile.add(rs.getString(6));   // address
                        profile.add(rs.getString(7));   // DATE_FORMAT(created_at, '%m/%d/%Y')
                        profile.add(rs.getString(8));   // starting_weight
                        profile.add(rs.getString(9));   // goal_weight
                        profile.add(rs.getString(10));   // password
                        validationMap.put("search", "successful");
                        validationMap.put("user", "found");
                        request.setAttribute("validationMap", validationMap);
                        request.getRequestDispatcher("/SearchUserResult.jsp").forward(request, response);
                    } else {
                        validationMap.put("user", "not found");
                        request.setAttribute("validationMap", validationMap);
                        request.getRequestDispatcher("/Search.jsp").forward(request, response);
                    }
                } catch (Exception e) {
                    validationMap.put("search", "failed");
                    request.setAttribute("validationMap", validationMap);
                    e.printStackTrace();
                }
            }
        } else {
            validationMap.put("search", "failed");
            request.setAttribute("validationMap", validationMap);
            request.getRequestDispatcher("/Search.jsp").forward(request, response);
        }
    }

    protected boolean validateFirstname(String firstname) {
        if (Pattern.compile(".*[0-9].*").matcher(firstname).find() || Pattern.compile(".*\\s.*").matcher(firstname).find()
                || firstname.contains("'") || firstname.contains(";")) {
            validationMap.put("firstname", "illegal characters");
            return false;
        } else if (firstname.equals(null) || firstname.equals("")) {
            validationMap.put("firstname", "empty");
            return false;
        } else if (firstname.length() > 30) {
            validationMap.put("firstname", "too long");
            return false;
        } else {
            validationMap.put("firstname", "OK");
            return true;
        }
    }

    protected boolean validateLastname(String lastname) {
        if (Pattern.compile(".*[0-9].*").matcher(lastname).find() || Pattern.compile(".*\\s.*").matcher(lastname).find()
                || lastname.contains("'") || lastname.contains(";")) {
            validationMap.put("lastname", "illegal characters");
            return false;
        } else if (lastname.equals(null) || lastname.equals("")) {
            validationMap.put("lastname", "empty");
            return false;
        } else if (lastname.length() > 30) {
            validationMap.put("lastname", "too long");
            return false;
        } else {
            validationMap.put("lastname", "OK");
            return true;
        }
    }

    protected int getRole(String firstname, String lastname) {    
        try {
            String sql = "SELECT role FROM users WHERE first_name = ? AND last_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, firstname);
            pstmt.setString(2, lastname);
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
}