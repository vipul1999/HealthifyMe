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
}
