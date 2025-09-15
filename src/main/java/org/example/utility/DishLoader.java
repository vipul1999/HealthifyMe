package org.example.utility;

import org.example.entity.Dish;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class DishLoader {

    public static List<Dish> loadDishesFromCSV(String filename) {
        List<Dish> dishes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String headerLine = br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                // Split CSV line (simple split, assumes no commas in fields except availability_regions)
                String[] parts = line.split(",", -1);
                if (parts.length < 12) continue; // Basic validation

                String name = parts[0];
                int calories = Integer.parseInt(parts[1]);
                Set<String> allergens = new HashSet<>();
                if (!parts[2].equalsIgnoreCase("None") && !parts[2].isEmpty()) {
                    String[] allergenArr = parts[2].split(";");
                    for (String allergen : allergenArr) {
                        allergens.add(allergen.trim().toLowerCase());
                    }
                }
                double protein = Double.parseDouble(parts[3]);
                double fiber = Double.parseDouble(parts[4]);
                double carbs = Double.parseDouble(parts[5]);
                int sodium = Integer.parseInt(parts[6]);
                String mealType = parts[7].toLowerCase();
                String cuisine = parts[8];
                boolean isVegetarian = Boolean.parseBoolean(parts[9]);
                boolean isVegan = Boolean.parseBoolean(parts[10]);

                Set<String> availabilityRegions = new HashSet<>();
                if (!parts[11].isEmpty()) {
                    String[] regions = parts[11].split(";");
                    for (String region : regions) {
                        availabilityRegions.add(region.trim());
                    }
                }

                Dish dish = new Dish(name, calories, allergens, protein, fiber, carbs, sodium, mealType, availabilityRegions, isVegetarian, isVegan);
                dishes.add(dish);
            }
        } catch (Exception e) {
            System.err.println("Error loading dishes: " + e.getMessage());
            e.printStackTrace();
        }
        return dishes;
    }

    // For testing
//    public static void main(String[] args) {
//        String filename = "C:\\Users\\vipul\\Downloads\\ticket\\fastDelivery\\HealthifyMe\\src\\main\\java\\org\\example\\dishes.csv";
//        List<Dish> dishes = loadDishesFromCSV(filename);
//        System.out.println("Loaded " + dishes.size() + " dishes:");
//        for (Dish dish : dishes) {
//            System.out.println(dish.getName() + " (" + dish.getCalories() + " kcal) - " + dish.getMealType());
//        }
//    }
}
