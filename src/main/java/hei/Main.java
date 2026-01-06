package hei;


import hei.model.Dish;

import java.sql.SQLException;

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



    }
}