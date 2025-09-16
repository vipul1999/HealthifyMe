package org.example;

import org.example.entity.Dish;
import org.example.entity.Profile;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MealPlannerTest {

    @Test
    void selectMealsHtml() {
        // This can be filled with a call to more detailed tests or left as a placeholder
    }

    private Dish createDish(String name, String mealType, int calories, double protein, double fiber, double carbs,
                            Set<String> allergens, Set<String> availabilityRegions,
                            boolean isVegetarian, boolean isVegan, int sodium) {
        return new Dish(
                name,
                calories,
                allergens,
                protein,
                fiber,
                carbs,
                sodium,
                mealType,
                availabilityRegions,
                isVegetarian,
                isVegan
        );
    }

    private Profile createProfile(int calorieTarget, String diet, Set<String> excludeAllergens, Map<String, Boolean> prefs) {
        Profile p = new Profile();
        p.setCalorieTarget(calorieTarget);
        p.setDiet(diet);
        p.setExcludeAllergens(excludeAllergens);
        p.setPreferences(prefs);
        return p;
    }

    @Test
    public void testSelectMealsHtml_basic() {
        List<Dish> dishes = new ArrayList<>();
        dishes.add(createDish("Oatmeal", "breakfast", 300, 10, 5, 30,
                Set.of(), Set.of("US"), true, false, 50));
        dishes.add(createDish("Chicken Salad", "lunch", 400, 35, 3, 10,
                Set.of(), Set.of("US"), false, false, 80));
        dishes.add(createDish("Steamed Veggies", "dinner", 350, 5, 6, 15,
                Set.of(), Set.of("US"), true, true, 60));
        dishes.add(createDish("Fruit Snack", "snack", 150, 2, 4, 20,
                Set.of(), Set.of("US"), true, true, 10));

        Profile profile = createProfile(1200, "omnivore", Set.of(), Map.of());

        String html = MealPlanner.selectMealsHtml(dishes, profile);

        assertNotNull(html);
        assertTrue(html.contains("Oatmeal"));
        assertTrue(html.contains("Chicken Salad"));
        assertTrue(html.contains("Steamed Veggies"));
        assertTrue(html.contains("Fruit Snack"));
        assertTrue(html.contains("Total calories consumed"));
    }

    @Test
    public void testFilterByAllergens_excludesCorrectly() {
        Dish d1 = createDish("Fish Dish", "dinner", 400, 25, 3, 10,
                Set.of("fish"), Set.of("US"), false, false, 90);
        Dish d2 = createDish("Veg Dish", "dinner", 300, 10, 5, 30,
                Set.of(), Set.of("US"), true, false, 40);

        Set<Dish> filtered = invokeFilterByAllergens(List.of(d1, d2), Set.of("fish"));

        assertTrue(filtered.contains(d2));
        assertFalse(filtered.contains(d1));
    }

    private Set<Dish> invokeFilterByAllergens(List<Dish> dishes, Set<String> excludeAllergens) {
        try {
            var method = MealPlanner.class.getDeclaredMethod("filterByAllergens", List.class, Set.class);
            method.setAccessible(true);
            return (Set<Dish>) method.invoke(null, dishes, excludeAllergens);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
            return Collections.emptySet();
        }
    }

    @Test
    public void testDietFallbackVeganToVegetarian() {
        Dish veganDish = createDish("Vegan Salad", "lunch", 250, 10, 4, 15, Set.of(), Set.of("US"), true, true, 20);
        Dish vegDish = createDish("Cheese Sandwich", "lunch", 300, 15, 3, 25, Set.of(), Set.of("US"), true, false, 25);
        List<Dish> dishes = List.of(vegDish);
        Profile profile = createProfile(1000, "vegan", Set.of(), Map.of());

        Set<Dish> filtered = invokeFilterByDietWithFallback(dishes, profile);

        assertTrue(filtered.contains(vegDish)); // fallback allows vegetarian
        assertFalse(filtered.contains(veganDish)); // vegan dish not present in list
    }

    private Set<Dish> invokeFilterByDietWithFallback(List<Dish> dishes, Profile profile) {
        try {
            var method = MealPlanner.class.getDeclaredMethod("filterByDietWithFallback", Set.class, Profile.class);
            method.setAccessible(true);
            return (Set<Dish>) method.invoke(null, new HashSet<>(dishes), profile);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
            return Collections.emptySet();
        }
    }

    @Test
    public void testPrepareHtmlOutput_containsAllMealTypes() {
        Dish breakfast = createDish("Pancakes", "breakfast", 350, 8, 2, 40, Set.of(), Set.of("US"), true, false, 45);
        Dish lunch = createDish("Turkey Wrap", "lunch", 450, 30, 3, 20, Set.of(), Set.of("US"), false, false, 60);
        Dish dinner = createDish("Grilled Fish", "dinner", 500, 40, 4, 10, Set.of("fish"), Set.of("US"), false, false, 70);
        Dish snack1 = createDish("Nuts", "snack", 200, 6, 5, 10, Set.of(), Set.of("US"), true, true, 15);
        Dish snack2 = createDish("Fruit", "snack", 100, 1, 3, 20, Set.of(), Set.of("US"), true, true, 10);

        MealPlanner.AssignedMeals assigned = new MealPlanner.AssignedMeals();
        assigned.mainMeals.put("breakfast", new MealPlanner.AssignedDish(breakfast, "breakfast"));
        assigned.mainMeals.put("lunch", new MealPlanner.AssignedDish(lunch, "lunch"));
        assigned.mainMeals.put("dinner", new MealPlanner.AssignedDish(dinner, "dinner"));
        assigned.snacks.add(new MealPlanner.AssignedDish(snack1, "snack"));
        assigned.snacks.add(new MealPlanner.AssignedDish(snack2, "snack"));

        Profile profile = createProfile(2000, "omnivore", Set.of(), Map.of());

        String html = invokePrepareHtmlOutput(assigned, profile);

        assert html != null;
        assertTrue(html.contains("Pancakes"));
        assertTrue(html.contains("Turkey Wrap"));
        assertTrue(html.contains("Grilled Fish"));
        assertTrue(html.contains("Nuts"));
        assertTrue(html.contains("Fruit"));
        assertTrue(html.contains("Total calories consumed"));
    }

    private String invokePrepareHtmlOutput(MealPlanner.AssignedMeals assignedMeals, Profile profile) {
        try {
            var method = MealPlanner.class.getDeclaredMethod("prepareHtmlOutput", MealPlanner.AssignedMeals.class, Profile.class);
            method.setAccessible(true);
            return (String) method.invoke(null, assignedMeals, profile);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
            return null;
        }
    }
}



