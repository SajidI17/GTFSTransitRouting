package dev.sajidislam;
import java.sql.*;


public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/OCGTFS"; //Change OCGTFS to the name of the database you have
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "admin";

    public static void main(String[] args) {

        try{
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Hello, World!");
    }
}