package hei;

import hei.model.Dish;
import hei.model.DishIngredient;
import hei.model.Ingredient;
import hei.type.CategoryEnum;
import hei.type.DishTypeEnum;
import hei.type.UnitEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final DBConnection dbConnection = new DBConnection();


    public Dish findDishById(Integer id) {
        Connection connection = dbConnection.getDBConnection();

        try {
            PreparedStatement ps = connection.prepareStatement("""
                SELECT id, name, dish_type, selling_price
                FROM dish
                WHERE id = ?
            """);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("Dish not found with id=" + id);
            }

            Dish dish = new Dish();
            dish.setId(rs.getInt("id"));
            dish.setName(rs.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
            dish.setSellingPrice(rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"));

            dish.setDishIngredients(findDishIngredientsByDishId(id));
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving dish", e);
        } finally {
            dbConnection.attemptCloseDBConnection(connection);
        }
    }

    public Dish saveDish(Dish dishToSave) {
        if (dishToSave == null) {
            throw new IllegalArgumentException("Dish to save cannot be null");
        }

        String upsertDishSql = """
        INSERT INTO dish (id, name, dish_type, selling_price)
        VALUES (?, ?, ?::dish_types, ?)
        ON CONFLICT (id) DO UPDATE
        SET name = EXCLUDED.name,
            dish_type = EXCLUDED.dish_type,
            selling_price = EXCLUDED.selling_price
        RETURNING id
    """;

        Connection con = null;
        PreparedStatement psDish = null;
        ResultSet rsDish = null;

        try {
            con = dbConnection.getDBConnection();
            con.setAutoCommit(false);

            psDish = con.prepareStatement(upsertDishSql);

            // ID (insert or update)
            if (dishToSave.getId() != null) {
                psDish.setInt(1, dishToSave.getId());
            } else {
                psDish.setNull(1, Types.INTEGER);
            }

            psDish.setString(2, dishToSave.getName());
            psDish.setString(3, dishToSave.getDishType().name());

            if (dishToSave.getSellingPrice() != null) {
                psDish.setDouble(4, dishToSave.getSellingPrice());
            } else {
                psDish.setNull(4, Types.NUMERIC);
            }

            rsDish = psDish.executeQuery();
            if (!rsDish.next()) {
                throw new RuntimeException("Error while saving dish: " + dishToSave.getName());
            }

            Integer savedDishId = rsDish.getInt("id");

            // ===== Dish ingredients =====
            List<DishIngredient> dishIngredients = dishToSave.getDishIngredients();

            detachDishIngredients(con, savedDishId);
            attachDishIngredients(con, savedDishId, dishIngredients);

            con.commit();
            return findDishById(savedDishId);

        } catch (SQLException e) {
            try {
                if (!con.isClosed()) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException("Failed to save dish " + dishToSave.getName(), e);

        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException ignored) {}
            dbConnection.attemptCloseDBConnection(rsDish, psDish, con);
        }
    }


    public List<Ingredient> findIngredients(int page, int size) {
        Connection connection = dbConnection.getDBConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try {
            PreparedStatement ps = connection.prepareStatement("""
                SELECT id, name, price, category
                FROM ingredient
                ORDER BY id
                LIMIT ? OFFSET ?
            """);
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
            dbConnection.attemptCloseDBConnection(connection);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        Connection connection = dbConnection.getDBConnection();

        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO ingredient (name, price, category)
                VALUES (?, ?, ?::category_type)
            """);

            for (Ingredient ing : newIngredients) {
                ps.setString(1, ing.getName());
                ps.setDouble(2, ing.getPrice());
                ps.setString(3, ing.getCategory().name());
                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();
            return newIngredients;

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Failed to create ingredients", e);
        } finally {
            dbConnection.attemptCloseDBConnection(connection);
        }
    }


    public List<Dish> findDishsByIngredientName(String ingredientName) {
        Connection connection = dbConnection.getDBConnection();

        try {
            PreparedStatement ps = connection.prepareStatement("""
                SELECT DISTINCT d.id
                FROM dish d
                JOIN dish_ingredient di ON d.id = di.id_dish
                JOIN ingredient i ON di.id_ingredient = i.id
                WHERE i.name ILIKE ?
            """);

            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();

            List<Dish> dishes = new ArrayList<>();
            while (rs.next()) {
                dishes.add(findDishById(rs.getInt("id")));
            }
            return dishes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.attemptCloseDBConnection(connection);
        }
    }


    public List<DishIngredient> findDishIngredientsByDishId(Integer dishId) {
        Connection connection = dbConnection.getDBConnection();
        List<DishIngredient> list = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement("""
                SELECT di.id,
                       di.quantity_required,
                       di.unit,
                       i.id AS ing_id,
                       i.name,
                       i.price,
                       i.category
                FROM dish_ingredient di
                JOIN ingredient i ON di.id_ingredient = i.id
                WHERE di.id_dish = ?
            """);

            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient(
                        rs.getInt("ing_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                );

                DishIngredient di = new DishIngredient();
                di.setId(rs.getInt("id"));
                di.setIngredient(ingredient);
                di.setQuantityRequired(rs.getDouble("quantity_required"));
                di.setUnit(UnitEnum.valueOf(rs.getString("unit")));

                list.add(di);
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.attemptCloseDBConnection(connection);
        }
    }

    private void detachDishIngredients(Connection con, Integer dishId) throws SQLException {
        PreparedStatement ps = con.prepareStatement("""
        DELETE FROM dish_ingredient WHERE id_dish = ?
    """);
        ps.setInt(1, dishId);
        ps.executeUpdate();
    }
    private void attachDishIngredients(
            Connection con,
            Integer dishId,
            List<DishIngredient> dishIngredients
    ) throws SQLException {

        if (dishIngredients == null || dishIngredients.isEmpty()) return;

        PreparedStatement ps = con.prepareStatement("""
        INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
        VALUES (?, ?, ?, ?::unit_type)
    """);

        for (DishIngredient di : dishIngredients) {
            ps.setInt(1, dishId);
            ps.setInt(2, di.getIngredient().getId());
            ps.setDouble(3, di.getQuantityRequired());
            ps.setString(4, di.getUnit().name());
            ps.addBatch();
        }

        ps.executeBatch();
    }
}


