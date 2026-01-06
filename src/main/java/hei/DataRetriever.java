package hei;

import hei.model.Dish;
import hei.model.Ingredient;
import hei.type.CategoryEnum;
import hei.type.DishTypeEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final DBConnection dbConnection = new DBConnection();

    Dish findDishById(Integer id) {
        Connection dataBaseConnection = dbConnection.getDBConnection();

        Dish dish = new Dish();
        try{
            String sqlDish = "select id, name, dish_type from dish where id = ?";
            PreparedStatement psDish = dataBaseConnection.prepareStatement(sqlDish);
            psDish.setInt(1, id);
            ResultSet resultSetDish = psDish.executeQuery();

            if(resultSetDish.next()){
                DishTypeEnum dishType = DishTypeEnum.valueOf(resultSetDish.getString("dish_type"));

                dish.setId(resultSetDish.getInt("id"));
                dish.setName(resultSetDish.getString("name"));
                dish.setDishType(dishType);
            }else {
                return null;
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

    List<Ingredient> findIngredients(int page, int size){
        Connection dataBaseConnection = dbConnection.getDBConnection();
        List<Ingredient> ingredientList = new ArrayList<>();
        int offset = (page - 1) * size;

        try {
            String sql = "select id, name, price, price ,category from ingredient order by id limit ? offset ?";
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
                ingredient.setDish(ingredient.getDish());
                ingredientList.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        dbConnection.closeConnection(dataBaseConnection);
        return ingredientList;
    }
}
