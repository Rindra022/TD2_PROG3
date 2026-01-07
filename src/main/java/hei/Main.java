package hei;


import hei.model.Dish;
import hei.model.Ingredient;
import hei.type.CategoryEnum;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args){
        DataRetriever dataRetriever = new DataRetriever();
        System.out.println("//--Find Dish By Id--//");
        System.out.println(dataRetriever.findDishById(1));
        System.out.println(dataRetriever.findDishById(99));
        System.out.println("\n");

        System.out.println("//--Find Ingredients--//");
        System.out.println(dataRetriever.findIngredients(2,2));
        System.out.println(dataRetriever.findIngredients(3, 5));
        System.out.println("\n");

        System.out.println("//--Create Ingredients--//");
        List<Ingredient> ingredients= new ArrayList<>();
        Ingredient ing1 = new Ingredient(6,"Fromage", 1200.0, CategoryEnum.DAIRY);
        Ingredient ing2 = new Ingredient(7, "Oignon", 500.0, CategoryEnum.VEGETABLE);
        ingredients.add(ing1);
        ingredients.add(ing2);
        List<Ingredient> saved1 = dataRetriever.createIngredients(ingredients);
        System.out.println("Saved ingredients: " + saved1);
        System.out.println("\n");

        System.out.println("//---------------------------------------");
        Ingredient ing3 = new Ingredient(8,"Carotte", 2000.0, CategoryEnum.VEGETABLE);
        Ingredient ing4 = new Ingredient(9, "Laitue", 2000.0, CategoryEnum.VEGETABLE);
        ingredients.add(ing3);
        ingredients.add(ing4);
        List<Ingredient> saved2 = dataRetriever.createIngredients(ingredients);
        System.out.println("Saved ingredients: " + saved2);


    }
}