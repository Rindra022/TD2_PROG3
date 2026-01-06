package hei;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private DBConnection() {

    }

    public static Connection getDBConnection(){
        try{
            String url =  System.getenv("JDBC_URL");
            String username = System.getenv("USERNAME");
            String password = System.getenv("PASSWORD");
            return DriverManager.getConnection(url, username, password);
        }catch (SQLException e){
            throw new RuntimeException("Error connection from database");
        }
    }

    public static void closeConnection(Connection dataBaseConnection){
        if(dataBaseConnection != null){
            try{
                dataBaseConnection.close();
                System.out.println("Connection closed successfully");
            }catch (SQLException e){
                System.out.println("Error attempt connection closed: "+ e);
                throw new RuntimeException(e);
            }
        }
    }
}
