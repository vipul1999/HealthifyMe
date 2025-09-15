package org.example;

import org.example.entity.Dish;
import org.example.entity.Profile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MealPlanner {

    public static String selectMealsHtml(List<Dish> dishes, Profile profile) {
        int calorieTarget = profile.getCalorieTarget();
        double minCalories = calorieTarget * 0.9;

        Set<Dish> filteredDishes = applyAllFilters(dishes, profile);

        Map<String, List<Dish>> dishesByMeal = groupDishesByMealType(filteredDishes);

        sortDishesByScore(dishesByMeal, profile.getPreferences());

        AssignedMeals assignedMeals = initialMealSelectionMapped(dishesByMeal);

        assignedMeals = fillMissingMealsBySwappingMapped(assignedMeals, dishesByMeal);

        assignedMeals = addSnacksToMeetCalorieTarget(assignedMeals, dishesByMeal, minCalories);

        return prepareHtmlOutput(assignedMeals, profile);
    }

    // Class to separate main meals (unique) and snacks (many)
    private static class AssignedMeals {
        Map<String, AssignedDish> mainMeals = new LinkedHashMap<>();
        List<AssignedDish> snacks = new ArrayList<>();
    }

    private static Set<Dish> applyAllFilters(List<Dish> dishes, Profile profile) {
        Set<Dish> filteredByAllergens = filterByAllergens(dishes, profile.getExcludeAllergens());
        Set<Dish> filteredByDiet = filterByDietWithFallback(filteredByAllergens, profile);
        Set<Dish> filteredByRegion = filterByRegionWithFallback(filteredByDiet, profile.getPreferredRegions());
        Set<Dish> filteredByNutrition = filterByNutritionalPreferences(filteredByRegion, profile.getPreferences());
        if (filteredByNutrition.isEmpty()) {
            System.out.println("No dishes meet strict nutritional preferences; relaxing nutrition constraints.");
            filteredByNutrition = filteredByRegion;
        }
        return filteredByNutrition;
    }

    private static Set<Dish> filterByAllergens(List<Dish> dishes, Set<String> excludeAllergens) {
        return dishes.stream()
                .filter(d -> Collections.disjoint(d.getAllergens(), excludeAllergens))
                .collect(Collectors.toSet());
    }

    private static Set<Dish> filterByDietWithFallback(Set<Dish> dishes, Profile profile) {
        Set<Dish> filtered = dishes.stream().filter(d -> dietFilter(d, profile)).collect(Collectors.toSet());
        if ("vegan".equalsIgnoreCase(profile.getDiet()) && filtered.isEmpty()) {
            System.out.println("No vegan dishes found; falling back to vegetarian dishes.");
            filtered = dishes.stream().filter(Dish::isVegetarian).collect(Collectors.toSet());
        }
        return filtered;
    }

    private static boolean dietFilter(Dish dish, Profile profile) {
        String diet = profile.getDiet().toLowerCase();
        switch (diet) {
            case "vegetarian": return dish.isVegetarian();
            case "vegan": return dish.isVegan();
            case "pescatarian":
                if (profile.getExcludeAllergens().contains("fish") || profile.getExcludeAllergens().contains("shellfish")) {
                    return dish.isVegetarian() || (!dish.getAllergens().contains("fish") && !dish.getAllergens().contains("shellfish"));
                } else {
                    return true;
                }
            default: return true;
        }
    }

    private static Set<Dish> filterByRegionWithFallback(Set<Dish> dishes, Set<String> preferredRegions) {
        Set<Dish> filtered = dishes.stream()
                .filter(d -> preferredRegions == null || preferredRegions.isEmpty()
                        || !Collections.disjoint(d.getAvailabilityRegions(), preferredRegions))
                .collect(Collectors.toSet());
        if (filtered.isEmpty()) {
            return new HashSet<>(dishes);
        }
        return filtered;
    }

    private static Set<Dish> filterByNutritionalPreferences(Set<Dish> dishes, Map<String, Boolean> prefs) {
        boolean highProtein = prefs.getOrDefault("high_protein", false);
        boolean highFiber = prefs.getOrDefault("high_fiber", false);
        boolean lowCarb = prefs.getOrDefault("low_carb", false);
        return dishes.stream().filter(d -> {
            if (highProtein && d.getProtein() < 15) return false;
            if (highFiber && d.getFiber() < 5) return false;
            if (lowCarb && d.getCarbs() >= 25) return false;
            return true;
        }).collect(Collectors.toSet());
    }

    private static Map<String, List<Dish>> groupDishesByMealType(Set<Dish> dishes) {
        Map<String, List<Dish>> map = new LinkedHashMap<>();
        map.put("breakfast", new ArrayList<>());
        map.put("lunch", new ArrayList<>());
        map.put("dinner", new ArrayList<>());
        map.put("snack", new ArrayList<>());
        for (Dish d : dishes) {
            String mt = d.getMealType().toLowerCase();
            if (map.containsKey(mt)) {
                map.get(mt).add(d);
            }
        }
        return map;
    }

    private static void sortDishesByScore(Map<String, List<Dish>> dishesByMeal, Map<String, Boolean> prefs) {
        for (List<Dish> dishList : dishesByMeal.values()) {
            dishList.sort((d1, d2) -> Double.compare(scoreDish(d2, prefs), scoreDish(d1, prefs)));
        }
    }

    private static AssignedMeals initialMealSelectionMapped(Map<String, List<Dish>> dishesByMeal) {
        AssignedMeals assigned = new AssignedMeals();
        for (String meal : Arrays.asList("breakfast", "lunch", "dinner")) {
            List<Dish> mealDishes = dishesByMeal.getOrDefault(meal, Collections.emptyList());
            if (!mealDishes.isEmpty()) {
                assigned.mainMeals.put(meal, new AssignedDish(mealDishes.get(0), meal));
            }
        }
        return assigned;
    }

    private static AssignedMeals fillMissingMealsBySwappingMapped(AssignedMeals assignedMeals, Map<String, List<Dish>> dishesByMeal) {
        Set<String> missingMeals = new LinkedHashSet<>(Arrays.asList("breakfast", "lunch", "dinner"));
        assignedMeals.mainMeals.keySet().forEach(missingMeals::remove);

        Set<Dish> used = assignedMeals.mainMeals.values().stream()
                .map(ad -> ad.dish)
                .collect(Collectors.toSet());
        used.addAll(assignedMeals.snacks.stream().map(ad -> ad.dish).collect(Collectors.toSet()));

        Map<String, List<String>> priorities = Map.of(
                "breakfast", Arrays.asList("lunch", "dinner", "snack"),
                "lunch", Arrays.asList("breakfast", "dinner", "snack"),
                "dinner", Arrays.asList("lunch", "breakfast", "snack")
        );

        for (String missing : missingMeals) {
            boolean filled = false;
            for (String source : priorities.getOrDefault(missing, Collections.emptyList())) {
                List<Dish> candidates = dishesByMeal.getOrDefault(source, Collections.emptyList());
                for (Dish candidate : candidates) {
                    if (!used.contains(candidate)) {
                        assignedMeals.mainMeals.put(missing, new AssignedDish(candidate, missing));
                        used.add(candidate);
                        System.out.println("Swapped '" + candidate.getName() + "' from " + source + " to fill " + missing);
                        filled = true;
                        break;
                    }
                }
                if (filled) break;
            }
            if (!filled) System.out.println("Could not fill missing meal: " + missing);
        }
        return assignedMeals;
    }

    private static AssignedMeals addSnacksToMeetCalorieTarget(AssignedMeals assignedMeals, Map<String, List<Dish>> dishesByMeal, double minCalories) {
        int totalCalories = assignedMeals.mainMeals.values().stream().mapToInt(ad -> ad.dish.getCalories()).sum()
                + assignedMeals.snacks.stream().mapToInt(ad -> ad.dish.getCalories()).sum();
        List<Dish> snacks = dishesByMeal.getOrDefault("snack", Collections.emptyList());
        for (Dish snack : snacks) {
            if (totalCalories >= minCalories) break;
            boolean usedInMain = assignedMeals.mainMeals.values().stream().anyMatch(ad -> ad.dish.equals(snack));
            boolean usedInSnacks = assignedMeals.snacks.stream().anyMatch(ad -> ad.dish.equals(snack));
            if (!usedInMain && !usedInSnacks) {
                assignedMeals.snacks.add(new AssignedDish(snack, "snack"));
                totalCalories += snack.getCalories();
            }
        }
        return assignedMeals;
    }

    private static String prepareHtmlOutput(AssignedMeals assignedMeals, Profile profile) {
        StringBuilder html = new StringBuilder();

        int totalCalories = 0;

        html.append("<h2>Meal Plan</h2>\n");
        html.append("<ul>\n");

        for (String meal : Arrays.asList("breakfast", "lunch", "dinner")) {
            AssignedDish ad = assignedMeals.mainMeals.get(meal);
            if (ad != null) {
                Dish dish = ad.dish;
                totalCalories += dish.getCalories();

                String rationale = generateRationale(dish, profile);
                if (!ad.assignedMealType.equalsIgnoreCase(dish.getMealType())) {
                    rationale += ", swapped from " + dish.getMealType();
                }

                html.append("<li><strong>").append(capitalize(meal)).append(":</strong> ")
                        .append(dish.getName())
                        .append(" (").append(dish.getCalories()).append(" kcal) - ")
                        .append(rationale)
                        .append("</li>\n");
            }
        }

        Set<Dish> printedSnacks = new HashSet<>();
        for (AssignedDish snack : assignedMeals.snacks) {
            if (printedSnacks.add(snack.dish)) {
                Dish dish = snack.dish;
                totalCalories += dish.getCalories();

                String rationale = generateRationale(dish, profile);
                if (!snack.assignedMealType.equalsIgnoreCase(dish.getMealType())) {
                    rationale += ", swapped from " + dish.getMealType();
                }

                html.append("<li><strong>Snack:</strong> ")
                        .append(dish.getName())
                        .append(" (").append(dish.getCalories()).append(" kcal) - ")
                        .append(rationale)
                        .append("</li>\n");
            }
        }

        html.append("</ul>\n");
        String summary = generateDaySummary(assignedMeals.mainMeals.size() + assignedMeals.snacks.size(),
                totalCalories, profile.getCalorieTarget(), profile.getPreferences());

        html.append("<p><strong>Total calories consumed:</strong> ").append(totalCalories).append(" kcal</p>\n");
        html.append("<p><em>").append(summary).append("</em></p>\n");

        return html.toString();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private static double scoreDish(Dish dish, Map<String, Boolean> prefs) {
        double score = 0;
        if (prefs.getOrDefault("high_protein", false)) score += dish.getProtein() * 2;
        if (prefs.getOrDefault("high_fiber", false)) score += dish.getFiber() * 1.5;
        if (prefs.getOrDefault("low_carb", false)) score += Math.max(0, 50 - dish.getCarbs());
        return score;
    }

    private static String generateRationale(Dish dish, Profile profile) {
        List<String> reasons = new ArrayList<>();
        Map<String, Boolean> prefs = profile.getPreferences();

        if (prefs.getOrDefault("high_protein", false) && dish.getProtein() > 15) reasons.add("high protein");
        if (prefs.getOrDefault("high_fiber", false) && dish.getFiber() > 5) reasons.add("high fiber");
        if (prefs.getOrDefault("low_carb", false) && dish.getCarbs() < 25) reasons.add("low carb");
        if (dish.getAllergens().contains("dairy") && !profile.getExcludeAllergens().contains("dairy")) reasons.add("includes dairy");

        if (dish.isVegan() && "vegan".equalsIgnoreCase(profile.getDiet())) reasons.add("vegan");
        else if (dish.isVegetarian() && "vegetarian".equalsIgnoreCase(profile.getDiet())) reasons.add("vegetarian");
        else if ("pescatarian".equalsIgnoreCase(profile.getDiet())
                && dish.getAllergens().stream().noneMatch(a -> a.equalsIgnoreCase("shellfish") || a.equalsIgnoreCase("fish"))) {
            reasons.add("pescatarian compliant");
        }

        if (reasons.isEmpty()) reasons.add("matches profile preferences");
        if (prefs.getOrDefault("low_carb", false) && dish.getCarbs() >= 25) reasons.add("moderate carbs for energy balance");

        return String.join(", ", reasons);
    }

    private static String generateDaySummary(int mealCount, int totalCalories, int calorieTarget, Map<String, Boolean> prefs) {
        String focus = "balanced nutrients";
        if (prefs.getOrDefault("high_fiber", false)) focus = "high fiber foods";
        else if (prefs.getOrDefault("high_protein", false)) focus = "high protein foods";
        else if (prefs.getOrDefault("low_carb", false)) focus = "low carb foods";

        if (mealCount == 0) {
            return "No suitable meals found to meet the profile preferences.";
        } else if (mealCount < 4) {
            return String.format("This meal plan provides approximately %d calories, below your target of %d calories. It includes %d meal(s) focusing on %s. Consider adjusting preferences or adding more dishes for better variety.",
                    totalCalories, calorieTarget, mealCount, focus);
        } else {
            return String.format("This meal plan provides approximately %d calories, closely matching the target of %d calories. It emphasizes %s to meet your dietary preferences. The plan includes a balanced selection of breakfast, lunch, dinner, and snacks to ensure variety and nutritional completeness.",
                    totalCalories, calorieTarget, focus);
        }
    }

    private static class AssignedDish {
        Dish dish;
        String assignedMealType;
        AssignedDish(Dish dish, String mealType) {
            this.dish = dish;
            this.assignedMealType = mealType;
        }
    }
}

