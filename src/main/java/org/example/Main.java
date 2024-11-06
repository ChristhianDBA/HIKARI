package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test");
        config.setUsername("sa");
        config.setPassword("");

        try (HikariDataSource ds = new HikariDataSource(config)) {
            executeSqlScript(ds);
            List<EmployeeDTO> employees = selectAllEmployees(ds);
            employees.forEach(System.out::println);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void executeSqlScript(HikariDataSource ds)
            throws SQLException {
        final var path = "src/main/resources/schema.sql";
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            InputStream inputStream = new FileInputStream(path);
            //read init sql
            String script = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            stmt.execute(script);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<EmployeeDTO> selectAllEmployees(HikariDataSource ds)
            throws SQLException {
        var employees = new ArrayList<EmployeeDTO>();

        try (Connection con = ds.getConnection();
             Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT e.id, e.name, e.email, e.department_id, d.name AS department_name " +
                    "FROM employees e " +
                    "JOIN departments d ON e.department_id = d.id");

            while (rs.next()) {
                EmployeeDTO employee = new EmployeeDTO();
                employee.setId(rs.getInt("id"));
                employee.setName(rs.getString("name"));
                employee.setEmail(rs.getString("email"));
                employee.setDepartmentId(rs.getInt("department_id"));
                employee.setDepartmentName(rs.getString("department_name"));
                employees.add(employee);
            }
        }

        return employees;
    }
}