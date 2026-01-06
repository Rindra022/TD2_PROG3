package hei;


import hei.model.Dish;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args){
        DataRetriever dataRetriever = new DataRetriever();
        System.out.println("//--Get All Categories--//");
        Dish dishById = dataRetriever.findDishById(1);
        System.out.println(dishById);

        System.out.println("\n");


    }
}