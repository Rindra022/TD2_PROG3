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

        Dish dish = null;
        try{
            String sqlDish = "select id, name, dish_type from dish where id = ?";
            PreparedStatement psDish = dataBaseConnection.prepareStatement(sqlDish);
            psDish.setInt(1, id);
            ResultSet resultSetDish = psDish.executeQuery();

            if(resultSetDish.next()){
                DishTypeEnum dishType = DishTypeEnum.valueOf(resultSetDish.getString("dish_type"));

                dish = new Dish();
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
}
