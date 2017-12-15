

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * Servlet implementation class SearchMedicalFiles
 */
@WebServlet("/diet")
public class SearchMedicalFiles extends HttpServlet {
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
            try {
                if (customer_type == 0) {
                    String sql = "SELECT"
                               + " DATE_FORMAT(pr.visit_date, '%m/%d/%Y'), p.id, u.first_name, u.middle_name, u.last_name, u.calories_from_vegetables, u.calories_from_meats, u.calories_from_carbs, u.calorie_goal"
                               + " b.name, r.name, r.floor, DATE_FORMAT(bu.start_date, '%m/%d/%Y'), DATE_FORMAT(bu.end_date, '%m/%d/%Y'),"
                               + " FROM users u"
                               + " INNER JOIN"
                               + " (SELECT id, user_id FROM customer) p"
                               + " ON u.id = p.user_id"
                               + " WHERE u.first_name = ? AND u.last_name = ?"
                               + " ORDER BY pr.visit_date";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, firstname);
                    pstmt.setString(2, lastname);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        rs.previous();
                        HashMap<Integer,String[]> map = new HashMap<Integer,String[]>();
                        int i = 0;
                        while (rs.next()) {
                            String array[] = new String[5];
                            array[0] = rs.getString(1);   // customer_records.visit_date
                            array[1] = String.valueOf(rs.getInt(2));   // customer.id
                            array[2] = rs.getString(3);   // users.first_name
                            array[3] = rs.getString(4);   // users.middle_name
                            array[4] = rs.getString(5);   // users.last_name
                            map.put(i, array);
                            i++;
                        }
                        validationMap.put("search", "successful");
                        validationMap.put("diet", "found");
                        request.setAttribute("validationMap", validationMap);
                        request.setAttribute("map", map);
                        request.getRequestDispatcher("/searchdietResult.jsp").forward(request, response);
                    } else {
                        validationMap.put("diet", "not found");
                        request.setAttribute("validationMap", validationMap);
                        request.getRequestDispatcher("/Search.jsp").forward(request, response);
                    }
                } else if (customer_type == 1) {
                    String sql = "SELECT"
                               + " DATE_FORMAT(pr.visit_date, '%m/%d/%Y'), p.id, u.first_name, u.middle_name, u.last_name,u.calories_from_vegetables, u.calories_from_meats, u.calories_from_carbs, u.calorie_goal"
                               + " b.name, r.name, r.floor, DATE_FORMAT(bu.start_date, '%m/%d/%Y'), DATE_FORMAT(bu.end_date, '%m/%d/%Y'),"
                               + " FROM users u"
                               + " INNER JOIN"
                               + " (SELECT id, user_id FROM customer) p"
                               + " ON u.id = p.user_id"
                               + " WHERE u.first_name = ? AND u.last_name = ?"
                               + " ORDER BY pr.visit_date";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, firstname);
                    pstmt.setString(2, lastname);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        rs.previous();
                        HashMap<Integer,String[]> map = new HashMap<Integer,String[]>();
                        int i = 0;
                        while (rs.next()) {
                            String array[] = new String[5];
                            array[0] = rs.getString(1);   // patient_records.visit_date
                            array[1] = String.valueOf(rs.getInt(2));   // patients.id
                            array[2] = rs.getString(3);   // users.first_name
                            array[3] = rs.getString(4);   // users.middle_name
                            array[4] = rs.getString(5);   // users.last_name
                            map.put(i, array);
                            i++;
                        }
                        validationMap.put("search", "successful");
                        validationMap.put("diet", "found");
                        request.setAttribute("validationMap", validationMap);
                        request.setAttribute("map", map);
                        request.getRequestDispatcher("/searchdietResult.jsp").forward(request, response);
                    } else {
                        validationMap.put("diet", "not found");
                        request.setAttribute("validationMap", validationMap);
                        request.getRequestDispatcher("/Search.jsp").forward(request, response);
                    }
                }
            } catch (Exception e) {
                validationMap.put("search", "failed");
                request.setAttribute("validationMap", validationMap);
                e.printStackTrace();
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

    protected int getcustomerType(String firstname, String lastname) {
        try {
            String sql = "SELECT type FROM customer WHERE user_id"
                       + " IN (SELECT id FROM users WHERE first_name = ? AND last_name = ?)";
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