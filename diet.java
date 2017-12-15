

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
 * Servlet implementation class NewMedicalFile
 */
@WebServlet("/diet")
public class diet extends HttpServlet {
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
        response.sendRedirect(request.getContextPath() + "/diet.jsp");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String customer_id = request.getParameter("customer_id");
        String calories_from_vegetables = request.getParameter("calories_from_vegetables");
        String calories_from_meats = request.getParameter("calories_from_meats");
        String calories_from_carbs = request.getParameter("calories_from_carbs");
        String calorie_goal = request.getParameter("calorie_goal");
        
        validationMap.clear();

        boolean checkcustomerId = validatecustomerId(customer_id);

                    /* Insert the form data to customer_records table. */
                    sql = "INSERT INTO"
                        + " customer_records (customer_id, created_at, updated_at, calories_from_vegetables, calories_from_meats, calories_from_carbs, calorie_goal)"
                        + " VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, STR_TO_DATE(?, '%m/%d/%Y'), ?, ?, ?, ?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, Integer.parseInt(customer_id));
                    pstmt.setInt(2, calories_from_vegetables);
                    pstmt.setString(3, calories_from_meats);
                    pstmt.setString(4, calories_from_carbs);
                    pstmt.setString(5, calorie_goal);
                    pstmt.executeUpdate();

                    pstmt.clearParameters();
                    
                        /* Get customer name of the inserted record avobe */
                        sql = "SELECT first_name, middle_name, last_name FROM users u"
                            + " INNER JOIN (SELECT id, user_id FROM customer) p"
                            + " ON u.id = p.user_id WHERE p.id = ?";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, Integer.parseInt(customer_id));
                        rs = pstmt.executeQuery();

                        if (rs.next()) {
                            firstname = rs.getString(1);
                            middlename = rs.getString(2);
                            lastname = rs.getString(3);
                        } else {
                            validationMap.put("registration", "failed");
                            request.setAttribute("validationMap", validationMap);
                            request.getRequestDispatcher("/diet.jsp").forward(request, response);
                        }

                        pstmt.clearParameters();

                    }

                    /* Get the registration information from inserted records above, and forward it to dietResult.jsp. */
                    sql = "SELECT"
                        + " p.customer_id, p.calories_from_vegetables, calories_from_meats, calories_from_carbs, calorie_goal"
                        + " FROM customer_records p"
                        + " INNER JOIN"
                        + " (SELECT customer_id, start_date, end_date, status FROM bed_usage) b";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, Integer.parseInt(customer_id));
                    pstmt.setInt(2, calories_from_vegetables);
                    pstmt.setInt(3, calories_from_meats);
                    pstmt.setInt(4, calories_from_carbs);
                    pstmt.setInt(5, calorie_goal);
                    rs = pstmt.executeQuery();

                        validationMap.put("registration", "successful");
                        request.setAttribute("validationMap", validationMap);
                        request.getRequestDispatcher("/dietResult.jsp").forward(request, response);
                    } else {
                        validationMap.put("registration", "failed");
                        request.setAttribute("validationMap", validationMap);
                        request.getRequestDispatcher("/diet.jsp").forward(request, response);
                    }

                    pstmt.clearParameters();

                }
            } catch (Exception e) {
                validationMap.put("registration", "failed");
                request.setAttribute("validationMap", validationMap);
                e.printStackTrace();
            }
        } else {
            validationMap.put("registration", "failed");
            request.setAttribute("validationMap", validationMap);
            request.getRequestDispatcher("/diet.jsp").forward(request, response);
        }
    }

    protected boolean validatecustomerId(String customer_id) {
        try {
            String sql = "SELECT id FROM customer WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, Integer.parseInt(customer_id));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                validationMap.put("customer_id", "OK");
                return true;
            } else {
                validationMap.put("customer_id", "not found");
                return false;
            }
        } catch (SQLException e) {
            validationMap.put("customer_id", "not found");
            log("SQLException:" + e.getMessage());
            return false;
        }
    }

    protected int getcustomerType(String customer_id) {
        try {
            String sql = "SELECT type FROM customer WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(customer_id));
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
}