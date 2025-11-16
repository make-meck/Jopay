package com.example.jopay;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/*
This class is responsible for handling all of the SQL query and updates to the database
 */


public class EmployeeDAO {
    static DatabaseConnector connect;


    public EmployeeDAO() {
        this.connect = new DatabaseConnector();
    }


    // find the employee by their employee_ID
    public Optional<Employee> findEmployeeId(String employeeId) throws SQLException {
        String query = """
            SELECT ei.employee_Id, ei.employee_FirstName, ei.employee_LastName,
                ei.employee_MiddleName, ei.employee_Department, ei.employment_Status,
                ei.employee_Title, ei.basic_Salary, ei.date_Hired, 
                ei.employee_DOB, ei.is_Active, ea.employee_password
            FROM employee_info ei
            JOIN employee_account ea ON ei.employee_Id = ea.employee_Id
            WHERE ei.employee_Id = ?
        """;

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getString("employee_Id"));
                employee.setFirstName(rs.getString("employee_FirstName"));
                employee.setLastName(rs.getString("employee_LastName"));
                employee.setMiddleName(rs.getString("employee_MiddleName"));
                employee.setDepartment(rs.getString("employee_Department"));
                employee.setEmploymentStatus(rs.getString("employment_Status"));
                employee.setTitle(rs.getString("employee_Title"));
                employee.setBasicSalary(rs.getDouble("basic_Salary"));
                employee.setDateHired(rs.getString("date_Hired"));

                // Handle DOB which might be null
                if (rs.getDate("employee_DOB") != null) {
                    employee.setDob(rs.getDate("employee_DOB").toLocalDate());
                }

                employee.setActive(rs.getBoolean("is_Active"));
                employee.setPassword(rs.getString("employee_password"));

                System.out.println("✓ Found employee: " + employee.getFullName() + " (Active: " + employee.isActive() + ")");

                return Optional.of(employee);
            } else {
                System.out.println("✗ Employee not found: " + employeeId);
                return Optional.empty();
            }

        } catch (SQLException e) {
            System.err.println("Error finding employee by ID: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    //updates the employee password
    public boolean updatePassword(int employeeId, String newPassword) throws SQLException {
        String updatePass = "UPDATE employee_account SET  employee_password = ?  WHERE employee_Id= ?";
        try (PreparedStatement stmt = connect.prepareStatement(updatePass)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, Integer.parseInt(String.valueOf(employeeId)));
            return stmt.executeUpdate() > 0;
        }
    }

    //makes the employee inactive
    public static void deactivateEmployee(int employeeId) throws SQLException {
        String sql = "UPDATE employee_info SET is_Active = 0 WHERE employee_Id= ?";
        PreparedStatement active = connect.prepareStatement(sql);
        active.setInt(1, employeeId);
        int updateRows = active.executeUpdate();
        active.close();

        if (updateRows == 0) {
            throw new SQLException("No employee found with ID" + employeeId);
        }
    }

    //makes the account inactive
    public static void deactivateAccount(int employeeId) throws SQLException {
        String sql = "UPDATE employee_account SET employee_password = NULL WHERE employee_Id =?";
        PreparedStatement deactivate = connect.prepareStatement(sql);
        deactivate.setInt(1, employeeId);
        deactivate.executeUpdate();
        deactivate.close();
    }


    //it checks if the empployee exists in the database
    public boolean employeeExists(String employeeId) throws SQLException {
        String check = "SELECT COUNT (*) FROM employee_account WHERE employee_Id= ?";
        try (PreparedStatement stmt = connect.prepareStatement(check)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        }
        return false;
    }

    //This gets all the Employees from the database
    public static List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employee_info ORDER BY employee_id";

        try (PreparedStatement stmt = connect.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setEmployeeId(rs.getString("employee_id"));
                emp.setFirstName(rs.getString("employee_FirstName"));
                emp.setLastName(rs.getString("employee_LastName"));
                emp.setDepartment(rs.getString("employee_Department"));
                emp.setEmploymentStatus(rs.getString("employment_Status"));
                employees.add(emp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }

    //This methods is used in the manage employee, where the admin needs to search by the employee's ID, Name, Department, and Status
    public static List<Employee> searchEmployees(String keyword) {
        List<Employee> employees = new ArrayList<>();

        // If keyword is empty, return all employees
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees();
        }

        String query = "SELECT * FROM employee_info WHERE " +
                "employee_id LIKE ? OR " +
                "employee_FirstName LIKE ? OR " +
                "employee_LastName LIKE ? OR " +
                "employee_Department LIKE ? OR " +
                "employment_Status LIKE ? OR " +
                "CONCAT(employee_FirstName, ' ', employee_LastName) LIKE ? " +
                "ORDER BY employee_id";

        try (PreparedStatement pstmt = connect.prepareStatement(query)) {

            String searchTerm = "%" + keyword + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            pstmt.setString(3, searchTerm);
            pstmt.setString(4, searchTerm);
            pstmt.setString(5, searchTerm);
            pstmt.setString(6, searchTerm); // add this for CONCAT

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee();
                    emp.setEmployeeId(rs.getString("employee_id"));
                    emp.setFirstName(rs.getString("employee_FirstName"));
                    emp.setLastName(rs.getString("employee_LastName"));
                    emp.setDepartment(rs.getString("employee_Department"));
                    emp.setEmploymentStatus(rs.getString("employment_Status"));
                    employees.add(emp);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }

    //This method is used to add employee from the manage employee to the database
    public static void addEmployee(Employee employee, String password) throws SQLException {
        connect.setAutoCommit(false);


        String insertEmployee = "INSERT INTO employee_info " +
                "(employee_Id, employee_FirstName, employee_LastName,employee_MiddleName, employee_DOB,employee_department, " +
                "employee_Title, basic_Salary, employment_Status, date_Hired) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = connect.prepareStatement(insertEmployee);
        stmt.setString(1, employee.getEmployeeId());
        stmt.setString(2, employee.getFirstName());
        stmt.setString(3, employee.getLastName());
        stmt.setString(4, employee.getMiddleName());
        stmt.setDate(5, java.sql.Date.valueOf(employee.getDOB()));
        stmt.setString(6, employee.getDepartment());
        stmt.setString(7, employee.getTitle());
        stmt.setDouble(8, employee.getBasicSalary());
        stmt.setString(9, employee.getStatus());
        stmt.setDate(10, java.sql.Date.valueOf(employee.getDateHired()));

        stmt.executeUpdate();


        String insertAccount = "INSERT INTO employee_account (employee_id, employee_password) VALUES (?, ?)";
        PreparedStatement accountStmt = connect.prepareStatement(insertAccount);
        accountStmt.setInt(1, Integer.parseInt(employee.getEmployeeId()));
        accountStmt.setString(2, password);
        accountStmt.executeUpdate();

        connect.commit();
    }
    //This get the next available employee ID based from the last Employee ID
    public static int getNextEmployeeId() throws SQLException {
        String query = "SELECT MAX(employee_Id) AS maxId FROM employee_info";
        PreparedStatement stmt = connect.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();


        int nextId = 6700002;
        if (rs.next()) {
            int maxId = rs.getInt("maxId");
            if (maxId != 0) {
                nextId = maxId + 1;
            }
        }
        rs.close();
        stmt.close();
        return nextId;
    }
    //This is used to find the employee by its Employee ID
    public static Employee getEmployeeById(int employeeId) throws SQLException {
        String query = """
                SELECT employee_Id, employee_FirstName, employee_LastName, 
                       employee_department, employee_Title, employment_Status
                FROM employee_info
                WHERE employee_Id = ? AND is_Active = 1
                """;

        PreparedStatement stmt = connect.prepareStatement(query);
        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();

        Employee employee = null;
        if (rs.next()) {
            employee = new Employee();
            employee.setEmployeeId(String.valueOf(rs.getInt("employee_Id")));
            employee.setFirstName(rs.getString("employee_FirstName"));
            employee.setLastName(rs.getString("employee_LastName"));
            employee.setDepartment(rs.getString("employee_department"));
            employee.setTitle(rs.getString("employee_Title"));
            employee.setEmploymentStatus(rs.getString("employment_Status"));
        }

        rs.close();
        stmt.close();
        return employee;
    }
    //It updates the employement status of the employee
    public static void updateEmploymentStatus() {
        String query = "SELECT employee_Id, date_Hired, employment_Status FROM employee_info";
        String updateQuery = "UPDATE employee_info SET employment_Status = ? WHERE employee_Id = ?";
        try (
                PreparedStatement stmt = connect.prepareStatement(query);
                ResultSet rs = stmt.executeQuery(query);
                PreparedStatement pstmt = connect.prepareStatement(updateQuery)) {

            LocalDate today = LocalDate.now();

            while (rs.next()) {
                int id = rs.getInt("employee_Id");
                LocalDate dateHired = rs.getDate("date_Hired").toLocalDate();
                String currentStatus = rs.getString("employment_Status");

                long monthsWorked = ChronoUnit.MONTHS.between(dateHired, today);

                String newStatus = (monthsWorked >= 6) ? "Regular" : "Probationary";

                if (!currentStatus.equalsIgnoreCase(newStatus)) {
                    pstmt.setString(1, newStatus);
                    pstmt.setInt(2, id);
                    pstmt.executeUpdate();
                    System.out.println("Employee " + id + " updated to " + newStatus);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    //It gets the number of active employee of the company
    public int getActiveEmployeeCount() {

        String query = "SELECT COUNT(*) AS employee_count FROM employee_info WHERE is_Active = 1";
        try (PreparedStatement stmt = connect.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("employee_count");
                return count;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Integer> getEmployeeDepartmentCounts() {
        Map<String, Integer> deptCounts = new HashMap<>();

        try {

            String deptQuery = "SELECT DISTINCT employee_Department FROM employee_info";
            try (PreparedStatement deptStmt = connect.prepareStatement(deptQuery);
                 ResultSet deptRs = deptStmt.executeQuery()) {
                while (deptRs.next()) {
                    String dept = deptRs.getString("employee_Department");
                    deptCounts.put(dept, 0); // default 0 count
                }
            }


            String countQuery = """
                        SELECT employee_Department, COUNT(*) AS count
                        FROM employee_info
                        WHERE is_Active = 1
                        GROUP BY employee_Department
                    """;

            try (PreparedStatement countStmt = connect.prepareStatement(countQuery);
                 ResultSet countRs = countStmt.executeQuery()) {
                while (countRs.next()) {
                    String dept = countRs.getString("employee_Department");
                    int count = countRs.getInt("count");
                    deptCounts.put(dept, count);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return deptCounts;
    }
    //This is for the admin dashboard, where it also tracks the attendance of the employee
    public Map<String, Integer> getWeeklyAttendanceSummary() {
        Map<String, Integer> summary = new HashMap<>();

        String query = """
        SELECT status, COUNT(*) AS count
        FROM time_log
        WHERE log_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        GROUP BY status
    """;

        try (PreparedStatement stmt = connect.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return summary;
    }

    //This is for getting the monthly attendance of the employee
    public Map<String, Integer> getEmployeeAttendanceSummary(int employeeId, int month, int year) {
        Map<String, Integer> summary = new HashMap<>();

        String query = """
        SELECT status, COUNT(*) AS count
        FROM time_log
        WHERE employee_Id = ?
        AND MONTH(log_date) = ?
        AND YEAR(log_date) = ?
        GROUP BY status
        """;

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                summary.put(status, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summary;
    }


}


