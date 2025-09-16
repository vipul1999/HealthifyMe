package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class Dish {
    private String name;
    private int calories;
    private Set<String> allergens;
    private double protein;
    private double fiber;
    private double carbs;
    private int sodium;
    private String mealType;
    private Set<String> availabilityRegions;
    private boolean isVegetarian;
    private boolean isVegan;

    public Dish(String name, String mealType, int calories, int protein, int fiber, int carbs, Set<String> allergens, Set<String> regions, boolean vegetarian, boolean vegan) {
    }
}
