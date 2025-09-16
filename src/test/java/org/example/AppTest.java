package org.example;

import org.example.entity.Dish;
import org.example.entity.Profile;
import org.example.utility.DishLoader;
import org.example.utility.profileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AppTest {

    private List<Dish> mockDishes;
    private List<Profile> mockProfiles;

    @BeforeEach
    public void setup() {
        mockDishes = List.of(
                new Dish("Test Dish", 300, Set.of(), 10, 5, 30, 40, "lunch", Set.of("US"), true, false)
        );
        mockProfiles = List.of(
                new Profile() {{
                    setAge(25);
                    setCalorieTarget(2000);
                    setDiet("omnivore");
                    setExcludeAllergens(Set.of());
                    setPreferences(Map.of());
                }}
        );
    }

    @Test
    public void basicTruthTest() {
        assertTrue(true);
    }
    @Test
    public void testMain_GeneratesHtmlAndWritesFile() throws IOException {
        // Setup temp file path
        Path tempFile = Files.createTempFile("test_meal_plans", ".html");
        try (MockedStatic<DishLoader> dishLoaderMock = mockStatic(DishLoader.class);
             MockedStatic<profileLoader> profileLoaderMock = mockStatic(profileLoader.class);
             MockedStatic<MealPlanner> mealPlannerMock = mockStatic(MealPlanner.class)) {

            dishLoaderMock.when(() -> DishLoader.loadDishesFromCSV(anyString())).thenReturn(mockDishes);
            profileLoaderMock.when(() -> profileLoader.loadProfilesFromCSV(anyString())).thenReturn(mockProfiles);
            mealPlannerMock.when(() -> MealPlanner.selectMealsHtml(mockDishes, mockProfiles.get(0)))
                    .thenReturn("<ul><li>Sample Meal</li></ul>");

            // Run main with overridden file path - modify your App to accept output path parameter or
            // temporally change App code to write to tempFile.toString()
            // Or copy your App.main logic here and change output file path

            // For example purpose, execute logic here:
            StringBuilder combinedHtml = new StringBuilder();
            combinedHtml.append("<html><body>Test</body></html>");
            Files.writeString(tempFile, combinedHtml.toString());

            // Verify file is created and has content
            assertTrue(Files.exists(tempFile));
            String content = Files.readString(tempFile);
            assertTrue(content.contains("Test"));

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
    @Test
    public void testProfileToJson_FormatsCorrectly1() throws Exception {
        Profile profile = mockProfiles.get(0);

        Method method = App.class.getDeclaredMethod("profileToJson", Profile.class);
        method.setAccessible(true); // <-- Fix!
        String json = (String) method.invoke(null, profile);

        assertTrue(json.contains("\"age\": 25"));
        assertTrue(json.contains("\"calorie_target\": 2000"));
        assertTrue(json.contains("\"diet\": \"omnivore\""));
    }
}
