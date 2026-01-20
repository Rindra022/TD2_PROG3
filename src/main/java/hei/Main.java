package hei;

import hei.model.Dish;

import java.sql.Connection;

public class Main {

    public static void main(String[] args) {

        DBConnection dbConnection = new DBConnection();
        DataRetriever dataRetriever = new DataRetriever();
        Connection connection = null;

        try {
            connection = dbConnection.getDBConnection();
            connection.setAutoCommit(false); // 🔒 début transaction

            System.out.println("=== TEST FIND DISH ===");

            Dish dish1 = dataRetriever.findDishById(1);
            System.out.println(dish1);
            System.out.println("Gross margin: " + dish1.getGrossMargin());

            Dish dish4 = dataRetriever.findDishById(4);
            System.out.println(dish4);
            System.out.println("Gross margin: " + dish4.getGrossMargin());

            System.out.println("\n=== TEST UPDATE (NON DEFINITIF) ===");

            dish1.setPrice(3000.0);
            dataRetriever.saveDish(dish1);

            Dish updatedDish = dataRetriever.findDishById(1);
            System.out.println("After update: " + updatedDish);

            System.out.println("\n=== ROLLBACK ===");
            connection.rollback(); // ❗ annule tout

            Dish rollbackDish = dataRetriever.findDishById(1);
            System.out.println("After rollback: " + rollbackDish);

        } catch (Exception e) {
            try {
                if (connection != null) connection.rollback();
            } catch (Exception ignored) {}
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                    dbConnection.closeConnection(connection);
                }
            } catch (Exception ignored) {}
        }
    }
}
