package com.example.jopay;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*this class will handle the employees of the company
transactions like delete, add, update and so on
 */
public class EmployeeDAO {
    private static DatabaseConnector connect;

    public EmployeeDAO(){
        this.connect= new DatabaseConnector();
    }

    //create or add new employee to the database

    public boolean createEmployee(Employee employee) throws SQLException {
        String addEmp = "INSERT INTO employee_account (employee_Id, employee_password) VALUES (? , ?) ";
            PreparedStatement stmt = connect.prepareStatement(addEmp);
            stmt.setString(1, employee.getEmployeeId());
            stmt.setString(2, employee.getPassword());
            return stmt.executeUpdate() >0;
    }
    // find the employee by their employee_ID
    public Optional<Employee> findEmployeeId(String employeeId) throws SQLException {
        String findEmp = """
        SELECT ea.employee_Id, ea.employee_password, ei.employee_FirstName, ei.employee_LastName
        FROM employee_account ea
        JOIN employee_info ei ON ea.employee_Id = ei.employee_Id
        WHERE ea.employee_Id = ?
    """;

        try (PreparedStatement stmt = connect.prepareStatement(findEmp)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getString("employee_Id"));
                employee.setPassword(rs.getString("employee_password"));
                employee.setFirstName(rs.getString("employee_FirstName"));
                employee.setLastName(rs.getString("employee_LastName"));
                return Optional.of(employee);
            }
        }
        return Optional.empty();
    }

    //updates the employee password
   public boolean updatePassword(int employeeId, String newPassword) throws SQLException {
        String updatePass = "UPDATE employee_account SET  employee_password = ?  WHERE employee_Id= ?";
        try(PreparedStatement stmt = connect.prepareStatement(updatePass)){
            stmt.setString(1, newPassword);
            stmt.setInt(2, Integer.parseInt(String.valueOf(employeeId)));
            return stmt.executeUpdate() >0;
        }
    }

    //delete an employee in the table of employee account
    public boolean deleteEmployee(String employeeId) throws SQLException {
        String delete = "DELETE FROM employee_account WHERE employee_Id=?";
        try(PreparedStatement stmt = connect.prepareStatement(delete)){
            stmt.setString(1, employeeId);
            return stmt.executeUpdate() > 0;
        }
    }

    //it checks if the empployee exists in the database
    public boolean employeeExists(String employeeId) throws SQLException {
        String check = "SELECT COUNT (*) FROM employee_account WHERE employee_Id= ?";
        try(PreparedStatement stmt = connect.prepareStatement(check)){
            stmt.setString(1, employeeId);
            ResultSet rs= stmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1)>0;
            }

        }
        return false;
    }

    public boolean validateLogin(String employeeId, String password) throws SQLException {
        String valiDate="SELECT employee_password FROM employee_account WHERE employee_Id=?";
        try (PreparedStatement stmt= connect.prepareStatement(valiDate)){
            stmt.setString(1, employeeId);
            ResultSet rs= stmt.executeQuery();
            if(rs.next()){
                String storedPassword= rs.getString("employee_id");
                //for now, using plain-text comparison
                //later, replace with Bcrypt.checkpw(password,storedpassword
                return storedPassword.equals(password);
            }
        }
        return false;
    }

    /*public List<Employee> searchEmployee(String query){
        List<Employee> employees= new ArrayList<>();
        String sql ="SELECT * FROM employee_info WHERE employee_Id LIKE ? OR employee_FirstName LIKE  ? OR employee_LastName LIKE ?";
        try(PreparedStatement stmt = connect.prepareStatement(sql)){
            try{
                int id= Integer.parseInt(query);
                stmt.setInt(1, "employee_Id");
            } catch(NumberFormatException e){
                stmt.setInt(1,-1);
            }
            stmt.setString(2, "%", + query + "%");
            stmt.setString(2, "%", + query + "%");
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    em
                }
            }
        }
    }*/

    public static void addEmployee(Employee employee, String tempPassword) throws SQLException {
        DatabaseConnector db = new DatabaseConnector();

        // 1️⃣ Insert Employee (without RETURN_GENERATED_KEYS)
        String insertEmployee = "INSERT INTO employee_info " +
                "(employee_Id, employee_FirstName, employee_LastName, employee_DOB, employement_Status, " +
                "employee_MiddleName, employee_department, employee_position, employee_Title, basic_Salary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = db.prepareStatement(insertEmployee);
        stmt.setString(1, employee.getEmployeeId());
        stmt.setString(2, employee.getFirstName());
        stmt.setString(3, employee.getLastName());
        stmt.setDate(4, java.sql.Date.valueOf(employee.getDOB()));
        stmt.setString(5, employee.getStatus());
        stmt.setString(6, employee.getMiddleName());
        stmt.setString(7, employee.getDepartment());
        stmt.setString(8, employee.getPosition());
        stmt.setString(9, employee.getTitle());
        stmt.setDouble(10, employee.getBasicSalary());

        stmt.executeUpdate();

        // 2️⃣ Get the generated employee ID (if your DB auto-increments a primary key)
        String getIdQuery = "SELECT LAST_INSERT_ID()"; // MySQL specific
        PreparedStatement idStmt = db.prepareStatement(getIdQuery);
        ResultSet keys = idStmt.executeQuery();
        int employeeId = 0;
        if (keys.next()) {
            employeeId = keys.getInt(1);
        }

        // 3️⃣ Insert temporary password
        String insertTemp = "INSERT INTO temp_passwords (employee_Id, temp_password) VALUES (?, ?)";
        PreparedStatement tempStmt = db.prepareStatement(insertTemp);
        tempStmt.setInt(1, employeeId);
        tempStmt.setString(2, tempPassword);
        tempStmt.executeUpdate();

        // 4️⃣ Close statements and result set
        keys.close();
        stmt.close();
        idStmt.close();
        tempStmt.close();
    }



}
