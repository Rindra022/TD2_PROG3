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
        Connection dataBaseConnection = dbConnection.getDBConnection();

        Dish dish = new Dish();
        try{
            String sqlDish = "select id, name, dish_type, price from dish where id = ?";
            PreparedStatement psDish = dataBaseConnection.prepareStatement(sqlDish);
            psDish.setInt(1, id);
            ResultSet resultSetDish = psDish.executeQuery();

            if(resultSetDish.next()){
                DishTypeEnum dishType = DishTypeEnum.valueOf(resultSetDish.getString("dish_type"));

                dish.setId(resultSetDish.getInt("id"));
                dish.setName(resultSetDish.getString("name"));
                dish.setDishType(dishType);
                dish.setPrice(resultSetDish.getDouble("price"));
            }else {
                throw new RuntimeException("Dish not found with id=" + id);
            }

            String sqlIngredient = "select id, name, price, category from ingredient where id_dish = ?";
            PreparedStatement psIngredient = dataBaseConnection.prepareStatement(sqlIngredient);
            psIngredient.setInt(1, dish.getId());
            ResultSet resultSetIngredient = psIngredient.executeQuery();

            List<Ingredient> ingredientList = new ArrayList<>();
            while (resultSetIngredient.next()){
                Ingredient ingredient = new Ingredient();
                CategoryEnum category = CategoryEnum.valueOf(resultSetIngredient.getString("category"));

                ingredient.setId(resultSetIngredient.getInt("id"));
                ingredient.setName(resultSetIngredient.getString("name"));
                ingredient.setPrice(resultSetIngredient.getDouble("price"));
                ingredient.setCategory(category);
                ingredient.setDish(dish);

                ingredientList.add(ingredient);
            }

            dish.setIngredients(ingredientList);
        }catch (SQLException e){
            throw new RuntimeException("Error while retrieving dish ",e);
        }

        dbConnection.closeConnection(dataBaseConnection);
        return dish;
    }

    public List<Ingredient> findIngredients(int page, int size){
        Connection dataBaseConnection = dbConnection.getDBConnection();
        List<Ingredient> ingredientList = new ArrayList<>();
        int offset = (page - 1) * size;

        try {
            String sql = "select id, name, price ,category from ingredient order by id limit ? offset ?";
            PreparedStatement ps = dataBaseConnection.prepareStatement(sql);
            ps.setInt(1, size);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredient.setDish(null);
                ingredientList.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        dbConnection.closeConnection(dataBaseConnection);
        return ingredientList;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients){
        List<Ingredient> savedIngredients = new ArrayList<>();

        Connection connection = null;

        try {
            connection = dbConnection.getDBConnection();
            connection.setAutoCommit(false);

            String checkSql = "select id from ingredient where name = ? AND id_dish = ?";
            String insertSql = "insert into ingredient (id, name, price, category, id_dish) VALUES (?,?,?,?::category_type,?)";

            PreparedStatement psCheck = connection.prepareStatement(checkSql);
            PreparedStatement psInsert =connection.prepareStatement(insertSql);

            for (Ingredient ing : newIngredients) {
                psCheck.setString(1, ing.getName());
                psCheck.setInt(2, ing.getDish().getId());
                ResultSet rsCheck = psCheck.executeQuery();

                if (rsCheck.next()) {
                    throw new RuntimeException("Ingredient already exists: " + ing.getName());
                }

                psInsert.setInt(1, ing.getId());
                psInsert.setString(2, ing.getName());
                psInsert.setDouble(3, ing.getPrice());
                psInsert.setString(4, ing.getCategory().name());
                psInsert.setInt(5, ing.getDish().getId());

                int affectedRows = psInsert.executeUpdate();
                if (affectedRows == 0) {
                    throw new RuntimeException("Failed to insert ingredient: " + ing.getName());
                }

                savedIngredients.add(ing);
            }

            connection.commit();

        }catch (SQLException | RuntimeException e){
            throw new RuntimeException("Failed to create ingredients: " + e.getMessage(), e);
        }finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }

        return savedIngredients;
    }

    public Dish saveDish(Dish dish) {
        Connection connection = dbConnection.getDBConnection();

        try {
            connection.setAutoCommit(false);

            if (dish.getId() == null) {
                String insertDish = "insert into dish (id, name, dish_type, price) values (?, ?, ?,?::dish_types)";
                PreparedStatement ps = connection.prepareStatement(insertDish);
                ps.setInt(1, dish.getId());
                ps.setString(2, dish.getName());
                ps.setString(4, dish.getDishType().name());
                if(dish.getPrice() != null){
                    ps.setDouble(3, dish.getPrice());
                }else{
                    ps.setNull(3, Types.NUMERIC);
                }
                ps.executeUpdate();
            } else {
                String updateDish = "update dish set name=?, dish_type=?::dish_types, price=? where id=?";
                PreparedStatement ps = connection.prepareStatement(updateDish);
                ps.setString(1, dish.getName());
                ps.setString(2, dish.getDishType().name());
                ps.setInt(4, dish.getId());

                if(dish.getPrice() != null){
                    ps.setDouble(3, dish.getPrice());
                }else{
                    ps.setNull(3, Types.NUMERIC);
                }
                ps.executeUpdate();

                PreparedStatement deleteLinks =
                        connection.prepareStatement("update ingredient set id_dish = null where id_dish = ?");
                deleteLinks.setInt(1, dish.getId());
                deleteLinks.executeUpdate();
            }

            if (dish.getIngredients() != null) {
                PreparedStatement psLink =
                        connection.prepareStatement("update ingredient set id_dish=? where id=?");

                for (Ingredient ing : dish.getIngredients()) {
                    psLink.setInt(1, dish.getId());
                    psLink.setInt(2, ing.getId());
                    psLink.executeUpdate();
                }
            }

            connection.commit();
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving dish", e);
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();
        Connection connection = dbConnection.getDBConnection();

        try {
            String sql = """
            select distinct d.id, d.name, d.dish_type, d.price
            from dish d
            join ingredient i on i.id_dish = d.id
            where lower(i.name) like lower(?)
        """;

            PreparedStatement ps = connection.prepareStatement(sql);
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

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dishes;
    }

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

        try {
            Connection connection = dbConnection.getDBConnection();
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

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ingredients;
    }

}
