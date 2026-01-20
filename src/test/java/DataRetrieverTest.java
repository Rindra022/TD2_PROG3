import hei.DataRetriever;
import hei.model.Dish;
import hei.model.Ingredient;
import hei.type.CategoryEnum;
import hei.type.DishTypeEnum;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DataRetrieverTest {

    private final DataRetriever dataRetriever = new DataRetriever();

    @Test
    public void test_findDishById_ok() {
        Dish dish = dataRetriever.findDishById(1);

        assertEquals("Salade fraîche", dish.getName());

        List<Ingredient> ingredients = dish.getIngredients();
        assertTrue(
                ingredients.stream().anyMatch(i -> i.getName().equals("Laitue"))
        );
        assertTrue(
                ingredients.stream().anyMatch(i -> i.getName().equals("Tomate"))
        );
    }

    @Test(expected = RuntimeException.class)
    public void test_findDishById_notFound() {
        dataRetriever.findDishById(999);
    }

    @Test
    public void test_findIngredients_page2_size2() {
        List<Ingredient> ingredients = dataRetriever.findIngredients(2, 2);

        assertEquals(2, ingredients.size());
        assertEquals("Poulet", ingredients.get(0).getName());
        assertEquals("Chocolat", ingredients.get(1).getName());
    }

    @Test
    public void test_findIngredients_emptyPage() {
        List<Ingredient> ingredients = dataRetriever.findIngredients(3, 5);
        assertTrue(ingredients.isEmpty());
    }

    @Test
    public void test_findDishsByIngredientName() {
        List<Dish> dishes = dataRetriever.findDishsByIngredientName("eur");

        assertEquals(1, dishes.size());
        assertEquals("Gâteau au chocolat", dishes.get(0).getName());
    }

    @Test
    public void test_findIngredientsByCriteria_categoryOnly() {
        List<Ingredient> ingredients =
                dataRetriever.findIngredientsByCriteria(
                        null,
                        CategoryEnum.VEGETABLE,
                        null,
                        1,
                        10
                );

        assertTrue(
                ingredients.stream().anyMatch(i -> i.getName().equals("Laitue"))
        );
        assertTrue(
                ingredients.stream().anyMatch(i -> i.getName().equals("Tomate"))
        );
    }

    @Test
    public void test_findIngredientsByCriteria_empty() {
        List<Ingredient> ingredients =
                dataRetriever.findIngredientsByCriteria(
                        "cho",
                        null,
                        "Sal",
                        1,
                        10
                );

        assertTrue(ingredients.isEmpty());
    }

    @Test
    public void test_findIngredientsByCriteria_chocolat() {
        List<Ingredient> ingredients =
                dataRetriever.findIngredientsByCriteria(
                        "cho",
                        null,
                        "gâteau",
                        1,
                        10
                );

        assertEquals(1, ingredients.size());
        assertEquals("Chocolat", ingredients.get(0).getName());
    }

    @Test
    public void test_createIngredients_ok() {
        Dish dish = dataRetriever.findDishById(1);

        Ingredient fromage = new Ingredient();
        fromage.setId(200);
        fromage.setName("Fromage");
        fromage.setCategory(CategoryEnum.DAIRY);
        fromage.setPrice(1200.0);
        fromage.setDish(dish);

        Ingredient oignon = new Ingredient();
        oignon.setId(201);
        oignon.setName("Oignon");
        oignon.setCategory(CategoryEnum.VEGETABLE);
        oignon.setPrice(500.0);
        oignon.setDish(dish);

        List<Ingredient> saved =
                dataRetriever.createIngredients(new ArrayList<>(Arrays.asList(fromage, oignon)));

        assertEquals(2, saved.size());
    }

    @Test(expected = RuntimeException.class)
    public void test_createIngredients_alreadyExists() {
        Dish dish = dataRetriever.findDishById(1);

        Ingredient carotte = new Ingredient();
        carotte.setId(300);
        carotte.setName("Carotte");
        carotte.setCategory(CategoryEnum.VEGETABLE);
        carotte.setPrice(2000.0);
        carotte.setDish(dish);

        Ingredient laitue = new Ingredient();
        laitue.setId(301);
        laitue.setName("Laitue");
        laitue.setCategory(CategoryEnum.VEGETABLE);
        laitue.setPrice(2000.0);
        laitue.setDish(dish);

        dataRetriever.createIngredients(List.of(carotte, laitue));
    }

    @Test
    public void test_saveDish_create() {
        Ingredient oignon = new Ingredient();
        oignon.setId(201); // existant

        Dish dish = new Dish();
        dish.setName("Soupe de légumes");
        dish.setDishType(DishTypeEnum.START);
        dish.setIngredients(List.of(oignon));

        Dish saved = dataRetriever.saveDish(dish);

        assertNotNull(saved.getId());
        assertEquals("Soupe de légumes", saved.getName());
    }

    @Test
    public void test_saveDish_update_addIngredients() {
        Dish dish = new Dish();
        dish.setId(1);
        dish.setName("Salade fraîche");
        dish.setDishType(DishTypeEnum.START);

        dish.setIngredients(List.of(
                new Ingredient(1),
                new Ingredient(2),
                new Ingredient(200),
                new Ingredient(201)
        ));

        Dish updated = dataRetriever.saveDish(dish);

        assertEquals("Salade fraîche", updated.getName());
    }

    @Test
    public void test_saveDish_update_removeIngredients() {
        Dish dish = new Dish();
        dish.setId(1);
        dish.setName("Salade de fromage");
        dish.setDishType(DishTypeEnum.START);

        dish.setIngredients(List.of(
                new Ingredient(200)
        ));

        Dish updated = dataRetriever.saveDish(dish);

        assertEquals("Salade de fromage", updated.getName());
    }
}
