package hei;

import hei.model.Dish;
import hei.model.Ingredient;
import hei.type.CategoryEnum;
import hei.type.DishTypeEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final DBConnection dbConnection = new DBConnection();

    public Dish findDishById(Integer id) {
        Connection connection = dbConnection.getDBConnection();

        try {
            String sql = """
                select id, name, dish_type, price
                from dish
                where id = ?
            """;

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("Dish not found with id=" + id);
            }

            Dish dish = new Dish();
            dish.setId(rs.getInt("id"));
            dish.setName(rs.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
            dish.setPrice(rs.getObject("price") == null ? null : rs.getDouble("price"));

            dish.setIngredients(findIngredientByDishId(id));

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    public Dish saveDish(Dish dish) {
        Connection connection = dbConnection.getDBConnection();

        try {
            connection.setAutoCommit(false);

            if (dish.getId() == null) {
                dish.setId(getNextSerialValue(connection, "dish", "id"));

                PreparedStatement ps = connection.prepareStatement("""
                    insert into dish (id, name, dish_type, price)
                    values (?, ?, ?::dish_types, ?)
                """);

                ps.setInt(1, dish.getId());
                ps.setString(2, dish.getName());
                ps.setString(3, dish.getDishType().name());

                if (dish.getPrice() != null) {
                    ps.setDouble(4, dish.getPrice());
                } else {
                    ps.setNull(4, Types.NUMERIC);
                }

                ps.executeUpdate();

            } else {
                PreparedStatement ps = connection.prepareStatement("""
                    update dish
                    set name=?, dish_type=?::dish_types, price=?
                    where id=?
                """);

                ps.setString(1, dish.getName());
                ps.setString(2, dish.getDishType().name());
                ps.setObject(3, dish.getPrice(), Types.NUMERIC);
                ps.setInt(4, dish.getId());
                ps.executeUpdate();
            }

            detachIngredients(connection, dish.getId());
            attachIngredients(connection, dish.getId(), dish.getIngredients());

            connection.commit();
            return findDishById(dish.getId());

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException(e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
            dbConnection.closeConnection(connection);
        }
    }

    private List<Ingredient> findIngredientByDishId(Integer dishId) {
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement("""
                select id, name, price, category
                from ingredient
                where id_dish = ?
            """);

            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ingredient ing = new Ingredient();
                ing.setId(rs.getInt("id"));
                ing.setName(rs.getString("name"));
                ing.setPrice(rs.getDouble("price"));
                ing.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredients.add(ing);
            }

            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }


    private void detachIngredients(Connection connection, Integer dishId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "update ingredient set id_dish = null where id_dish = ?"
        );
        ps.setInt(1, dishId);
        ps.executeUpdate();
    }


    private void attachIngredients(Connection connection, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {

        if (ingredients == null) return;

        PreparedStatement ps = connection.prepareStatement("""
            update ingredient
            set id_dish = ?
            where id = ?
        """);

        for (Ingredient ing : ingredients) {
            ps.setInt(1, dishId);
            ps.setInt(2, ing.getId());
            ps.addBatch();
        }

        ps.executeBatch();
    }


    private int getNextSerialValue(Connection conn, String table, String column) throws SQLException {
        String sql = "select nextval(pg_get_serial_sequence(?, ?))";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, table);
        ps.setString(2, column);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt(1);
    }
}
