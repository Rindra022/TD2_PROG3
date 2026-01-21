package hei.model;

import hei.type.DishTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private List<DishIngredient> dishIngredients;
    private Double sellingPrice;

    public Double getDishCost(){
        if(dishIngredients == null){
            return 0.0;
        }

        double total = 0.0;
        for (DishIngredient dishInngredient : dishIngredients) {
            total =+ dishInngredient.getCost();
        }
        return total;
    };

    public Double getGrossMargin(){
        if(sellingPrice == null){
            throw new RuntimeException("Price is null");
        }else {
            return sellingPrice - getDishCost();
        }
    }

    public Dish(){

    }

    public Dish(String name, DishTypeEnum dishType, List<DishIngredient> dishIngredients) {
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = dishIngredients;
    }

    public Dish(Integer id, String name, DishTypeEnum dishType, List<DishIngredient> dishIngredients, Double sellingPrice) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.dishIngredients = dishIngredients;
        this.sellingPrice = sellingPrice;
    }

    public Double getPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Double price) {
        this.sellingPrice = price;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Dish name can't be null");
        }
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> newDishIngredients) {
        if (this.dishIngredients == null) {
            this.dishIngredients = new ArrayList<>();
        } else {
            this.dishIngredients.clear();
        }
        if (newDishIngredients != null) {
            for (DishIngredient dishIn : newDishIngredients) {
                if (dishIn != null) {
                    dishIn.setDish(this);
                    this.dishIngredients.add(dishIn);
                }
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id) && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(dishIngredients, dish.dishIngredients) && Objects.equals(sellingPrice, dish.sellingPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, dishIngredients, sellingPrice);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", dishIngredients=" + dishIngredients +
                ", sellingPrice=" + sellingPrice +
                '}';
    }
}
