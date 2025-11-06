package com.example.jopay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;

public class AutheticationHandler {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    public Optional<Employee> authenticate(String employeeId, String password) {
        try {
            Optional<Employee> employeeOpt = employeeDAO.findEmployeeId(employeeId);
            if (employeeOpt.isEmpty()) {
                return Optional.empty(); // employee not found
            }

            Employee employee = employeeOpt.get();

            // Check password
            if (employee.getPassword().equals(password)) {
                return Optional.of(employee); // correct password
            } else {
                return Optional.empty(); // incorrect password
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}

// for hashed if kakayanin implementation nito
       /*String hashedInput =hashPassword (rawPassword);

       if(employee.getPassword().equals(hashedInput)){
           return Optional.of(employee);
       }
       return Optional.empty();
   }

   /*public static String hashPassword (String password){
       try{
           MessageDigest digest = MessageDigest.getInstance("SHA-256");
           byte[] encoded = digest.digest(password.getBytes(StandardCharsets.UTF_8));

           StringBuilder hex= new StringBuilder();
           for ( byte b: encoded){
               String hexChar = Integer.toHexString(0xff & b);
               if(hexChar.length()==1) hex.append('0');
               hex.append(hexChar);
           }
           return hex.toString();
       } catch (NoSuchAlgorithmException e){
           throw new RuntimeException("Error hashin password", e);
       }
   }
*/


