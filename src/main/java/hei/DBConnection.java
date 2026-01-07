package hei;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public Connection getDBConnection(){
        try{
            String url =  System.getenv("JDBC_URL");
            String username = System.getenv("USERNAME");
            String password = System.getenv("PASSWORD");
            return DriverManager.getConnection(url, username, password);
        }catch (SQLException e){
            throw new RuntimeException("Error connection from database", e);
        }
    }

    public void closeConnection(Connection dataBaseConnection){
        if(dataBaseConnection != null){
            try{
                dataBaseConnection.close();
            }catch (SQLException e){
                throw new RuntimeException("Error while closing database connection", e);
            }
        }
    }
}
