package org.example.utility;

import org.example.entity.Profile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class profileLoader {

    public static List<Profile> loadProfilesFromCSV(String filePath) {
        List<Profile> profiles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length < 8) {
                    System.err.println("Invalid line skipped: " + line);
                    continue;
                }
                try {
                    int age = Integer.parseInt(fields[0]);
                    int calorieTarget = Integer.parseInt(fields[1]);
                    String diet = fields[2];
                    Set<String> excludeAllergens = new HashSet<>();
                    if (!fields[3].isEmpty()) {
                        excludeAllergens = new HashSet<>(Arrays.asList(fields[3].split(";")));
                    }
                    boolean highProtein = Boolean.parseBoolean(fields[4]);
                    boolean highFiber = Boolean.parseBoolean(fields[5]);
                    boolean lowCarb = Boolean.parseBoolean(fields[6]);
                    Set<String> preferredRegions = new HashSet<>();
                    if (!fields[7].isEmpty()) {
                        preferredRegions = new HashSet<>(Arrays.asList(fields[7].split(";")));
                    }

                    Map<String, Boolean> preferences = new HashMap<>();
                    preferences.put("high_protein", highProtein);
                    preferences.put("high_fiber", highFiber);
                    preferences.put("low_carb", lowCarb);

                    Profile profile = new Profile(calorieTarget, excludeAllergens, diet, preferences, age, preferredRegions);
                    profiles.add(profile);
//                    System.out.println("Loaded Profile:");
//                    System.out.println(" Age: " + age + ", Diet: " + diet);
//                    System.out.println(" Calorie Target: " + calorieTarget);
//                    System.out.println(" Exclude Allergens: " + excludeAllergens);
//                    System.out.println(" Preferences: " + preferences);
//                    System.out.println(" Preferred Regions: " + preferredRegions);
//                    System.out.println("------------------------------------");

                } catch (Exception e) {
                    System.err.println("Error parsing line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }
        return profiles;
    }


    public static void processLargeProfileCSV(String filepath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String header = br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length < 8) continue; // validate

                try {
                    int age = Integer.parseInt(fields[0]);
                    int calorieTarget = Integer.parseInt(fields[1]);
                    String diet = fields[2];
                    Set<String> excludeAllergens = new HashSet<>(Arrays.asList(fields[3].split(";")));
                    boolean highProtein = Boolean.parseBoolean(fields[4]);
                    boolean highFiber = Boolean.parseBoolean(fields[5]);
                    boolean lowCarb = Boolean.parseBoolean(fields[6]);
                    Set<String> preferredRegions = new HashSet<>(Arrays.asList(fields[7].split(";")));

                    Map<String, Boolean> preferences = new HashMap<>();
                    preferences.put("high_protein", highProtein);
                    preferences.put("high_fiber", highFiber);
                    preferences.put("low_carb", lowCarb);

                    Profile profile = new Profile(calorieTarget, excludeAllergens, diet, preferences, age, preferredRegions);

                    // Process profile immediately or add to a bounded queue/db
                    System.out.println("Loaded profile for age " + age);

                } catch (Exception e) {
                    System.err.println("Skipping invalid line due to error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading profile CSV: " + e.getMessage());
        }
    }
}
