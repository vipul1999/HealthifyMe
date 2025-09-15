package org.example;

import org.example.entity.Dish;
import org.example.entity.Profile;
import org.example.utility.DishLoader;
import org.example.utility.profileLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        String dishFileAbsPath = "C:\\Users\\vipul\\Downloads\\ticket\\fastDelivery\\HealthifyMe\\src\\main\\java\\org\\example\\data\\dishes.csv";
        List<Dish> dishes = DishLoader.loadDishesFromCSV(dishFileAbsPath);
        System.out.println("Loaded " + dishes.size() + " dishes.");

        String patientFileAbsPath = "C:\\Users\\vipul\\Downloads\\ticket\\fastDelivery\\HealthifyMe\\src\\main\\java\\org\\example\\data\\profiles.csv";
        List<Profile> profiles = profileLoader.loadProfilesFromCSV(patientFileAbsPath);
        System.out.println("Loaded " + profiles.size() + " patient profiles.");

        StringBuilder combinedHtml = new StringBuilder();

        // Begin full html document
        combinedHtml.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"UTF-8\">\n")
                .append("<title>All Meal Plans</title>\n")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; padding: 20px; background:#f9f9f9 }")
                .append("h1,h2 {color:#2c3e50}")
                .append("pre { background:#ececec; padding:10px; border-radius:5px }")
                .append("section { margin-bottom: 50px; padding: 20px; border-bottom: 1px solid #ccc; }")
                .append("</style>\n</head>\n<body>\n");

        int profileCount = 1;
        for (Profile profile : profiles) {
            combinedHtml.append("<section>\n");
            combinedHtml.append("<h1>Profile ").append(profileCount).append("</h1>\n");
            combinedHtml.append("<h2>Details</h2>\n");
            combinedHtml.append("<pre>").append(profileToJson(profile)).append("</pre>\n");

            String mealPlanHtml = MealPlanner.selectMealsHtml(dishes, profile);

            combinedHtml.append(mealPlanHtml);
            combinedHtml.append("</section>\n");
            profileCount++;
        }

        combinedHtml.append("</body>\n</html>");

        // Write combined HTML once to a single file
        String outputFile = "all_meal_plans.html";
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(combinedHtml.toString());
            System.out.println("Saved combined meal plans to " + outputFile);
        } catch (IOException e) {
            System.err.println("Error writing combined HTML: " + e.getMessage());
        }
    }

    private static String profileToJson(Profile profile) {
        return String.format("{\n  \"age\": %d,\n  \"calorie_target\": %d,\n  \"diet\": \"%s\",\n  \"exclude_allergens\": %s,\n  \"preferences\": %s\n}",
                profile.getAge(),
                profile.getCalorieTarget(),
                profile.getDiet(),
                profile.getExcludeAllergens().toString(),
                profile.getPreferences().toString()
        );
    }
}