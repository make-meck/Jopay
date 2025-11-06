package com.example.jopay;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/*this class will handle the employees of the company
transactions like delete, add, update and so on
 */
public class EmployeeDAO {
    private DatabaseConnector connect;

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

}
