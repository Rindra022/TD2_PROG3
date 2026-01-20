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
            PreparedStatement psDish = connection.prepareStatement(
                    "select id, name, dish_type, price from dish where id = ?");
            psDish.setInt(1, id);
            ResultSet rsDish = psDish.executeQuery();

            if (!rsDish.next()) {
                throw new RuntimeException("Dish not found with id=" + id);
            }

            Dish dish = new Dish();
            dish.setId(rsDish.getInt("id"));
            dish.setName(rsDish.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(rsDish.getString("dish_type")));
            dish.setPrice(rsDish.getObject("price") == null
                    ? null : rsDish.getDouble("price"));

            dish.setIngredients(findIngredientByDishId(id));
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException("Error while retrieving dish", e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "select id, name, price, category from ingredient order by id limit ? offset ?");
            ps.setInt(1, size);
            ps.setInt(2, offset);

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


    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        List<Ingredient> savedIngredients = new ArrayList<>();
        Connection connection = null;

        try {
            connection = dbConnection.getDBConnection();
            connection.setAutoCommit(false);

            String insertSql =
                    "insert into ingredient (id, name, price, category, id_dish) " +
                            "values (?, ?, ?, ?::category_type, ?)";

            PreparedStatement ps = connection.prepareStatement(insertSql);

            for (Ingredient ing : newIngredients) {
                ps.setInt(1, ing.getId());
                ps.setString(2, ing.getName());
                ps.setDouble(3, ing.getPrice());
                ps.setString(4, ing.getCategory().name());
                ps.setInt(5, ing.getDish().getId());
                ps.executeUpdate();
                savedIngredients.add(ing);
            }

            connection.commit();
            return savedIngredients;

        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ignored) {}
            throw new RuntimeException("Failed to create ingredients", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /* =========================
       SAVE DISH (IMPROVED)
       ========================= */
    public Dish saveDish(Dish dish) {
        Connection connection = dbConnection.getDBConnection();

        try {
            connection.setAutoCommit(false);

            if (dish.getId() == null) {
                PreparedStatement ps = connection.prepareStatement(
                        "insert into dish (id, name, dish_type, price) values (?, ?, ?::dish_types, ?)");
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
                PreparedStatement ps = connection.prepareStatement(
                        "update dish set name=?, dish_type=?::dish_types, price=? where id=?");
                ps.setString(1, dish.getName());
                ps.setString(2, dish.getDishType().name());

                if (dish.getPrice() != null) {
                    ps.setDouble(3, dish.getPrice());
                } else {
                    ps.setNull(3, Types.NUMERIC);
                }

                ps.setInt(4, dish.getId());
                ps.executeUpdate();

                detachIngredients(connection, dish.getId());
            }

            attachIngredients(connection, dish);

            connection.commit();
            return dish;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {}
            throw new RuntimeException("Error saving dish", e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    /* =========================
       FIND DISH BY INGREDIENT NAME
       ========================= */
    public List<Dish> findDishsByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();
        Connection connection = dbConnection.getDBConnection();

        try {
            PreparedStatement ps = connection.prepareStatement("""
                select distinct d.id, d.name, d.dish_type, d.price
                from dish d
                join ingredient i on i.id_dish = d.id
                where lower(i.name) like lower(?)
            """);

            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                dish.setPrice(rs.getDouble("price"));
                dishes.add(dish);
            }

            return dishes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    /* =========================
       FIND INGREDIENTS BY CRITERIA
       ========================= */
    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size
    ) {
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        StringBuilder sql = new StringBuilder("""
            select i.id, i.name, i.price, i.category
            from ingredient i
            join dish d on i.id_dish = d.id
            where 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (ingredientName != null) {
            sql.append(" and lower(i.name) like lower(?)");
            params.add("%" + ingredientName + "%");
        }
        if (category != null) {
            sql.append(" and i.category = ?::category_type");
            params.add(category.name());
        }
        if (dishName != null) {
            sql.append(" and lower(d.name) like lower(?)");
            params.add("%" + dishName + "%");
        }

        sql.append(" limit ? offset ?");
        params.add(size);
        params.add(offset);

        Connection connection = dbConnection.getDBConnection();

        try {
            PreparedStatement ps = connection.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

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


    private List<Ingredient> findIngredientByDishId(Integer dishId) {
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "select id, name, price, category from ingredient where id_dish = ?");
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
        PreparedStatement ps =
                connection.prepareStatement("update ingredient set id_dish = null where id_dish = ?");
        ps.setInt(1, dishId);
        ps.executeUpdate();
    }

    private void attachIngredients(Connection connection, Dish dish) throws SQLException {
        if (dish.getIngredients() == null) return;

        PreparedStatement ps =
                connection.prepareStatement("update ingredient set id_dish=? where id=?");

        for (Ingredient ing : dish.getIngredients()) {
            ps.setInt(1, dish.getId());
            ps.setInt(2, ing.getId());
            ps.addBatch();
        }
        ps.executeBatch();
    }
}
