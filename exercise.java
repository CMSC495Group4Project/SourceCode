

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
        response.sendRedirect(request.getContextPath() + "/exercise.jsp");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        String customer_id = request.getParameter("customer_id");
        String days_per_week = request.getParameter("days_per_week");
        String cardio_time = request.getParameter("cardio_time");
        String exercise_time_goal = request.getParameter("exercise_time_goal");
        String daily_weight = request.getParameter("daily_weight");
        
        validationMap.clear();

        boolean checkcustomerId = validatecustomerId(customer_id);

                    /* Insert the form data to customer_records table. */
                    sql = "INSERT INTO"
                        + " customer_records (customer_id, created_at, updated_at, days_per_week, cardio_time, exercise_time_goal, daily_weight)"
                        + " VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, STR_TO_DATE(?, '%m/%d/%Y'), ?, ?, ?, ?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, Integer.parseInt(customer_id));
                    pstmt.setInt(2, days_per_week);
                    pstmt.setString(3, cardio_time);
                    pstmt.setString(4, exercise_time_goal);
                    pstmt.setString(5, daily_weight);
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
                            request.getRequestDispatcher("/exercise.jsp").forward(request, response);
                        }

                        pstmt.clearParameters();

                    }

                    /* Get the registration information from inserted records above, and forward it to exerciseResult.jsp. */
                    sql = "SELECT"
                        + " p.customer_id, p.days_per_week, cardio_time, exercise_time_goal, daily_weight"
                        + " FROM customer_records p"
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, Integer.parseInt(customer_id));
                    pstmt.setInt(2, days_per_week);
                    pstmt.setInt(3, cardio_time);
                    pstmt.setInt(4, exercise_time_goal);
                    pstmt.setInt(5, daily_weight);
                    rs = pstmt.executeQuery();

                        validationMap.put("registration", "successful");
                        request.setAttribute("validationMap", validationMap);
                        request.getRequestDispatcher("/exerciseResult.jsp").forward(request, response);
                    } else {
                        validationMap.put("registration", "failed");
                        request.setAttribute("validationMap", validationMap);
                        request.getRequestDispatcher("/exercise.jsp").forward(request, response);
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
            request.getRequestDispatcher("/exercise.jsp").forward(request, response);
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