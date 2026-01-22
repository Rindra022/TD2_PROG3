package hei.model;

import hei.type.CategoryEnum;
import hei.type.UnitEnum;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList;

    public Ingredient(){}

    public Ingredient(Integer id){
        this.id = id;
    }
    public Ingredient(String name, Double price, CategoryEnum category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }
    public Ingredient(Integer id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public StockValue getStockValueAT(Instant t){
        if(stockMovementList == null || stockMovementList.isEmpty()){
            return new StockValue(0.0, UnitEnum.KG);
        }

        double total = 0.0;
        UnitEnum unit = stockMovementList.get(0).getValue().getUnit();

        for (StockMovement sm : stockMovementList) {
            if (!sm.getCreationDatetime().isAfter(t)) {
                total += sm.getValue().getQuantity();
            }
        }

        return new StockValue(total, unit);

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
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Ingredient price cannot be negative");
        }
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(price, that.price)
                && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, category);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                '}';
    }
}
