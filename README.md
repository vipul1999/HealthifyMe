# HealthifyMe Meal Planner

## Overview

HealthifyMe is a Java-based personalized meal planner application designed to generate daily meal plans for patients based on their individual profiles. It factors in patient-specific details such as calorie targets, dietary restrictions (vegetarian, vegan, pescatarian), allergen exclusions, and nutritional preferences like high protein, low carb, and high fiber.

The application reads dishes and patient profiles from CSV files, processes and filters the data, intelligently selects meals, and generates detailed HTML meal plans suitable for review and sharing.

***

## Features

- **Personalized Meal Plans:** Customizes meals based on patient profiles including dietary restrictions and allergens.
- **Nutritional Preference Filtering:** Supports high protein, low carb, and high fiber preferences.
- **Meal Type Grouping:** Organizes dishes into breakfast, lunch, dinner, and snacks categories.
- **Fallback & Swapping:** Automatically fills missing meals by swapping from other meal types.
- **Scoring System:** Scores dishes for optimal selection based on patient preferences.
- **Batch Processing:** Processes multiple patients and aggregates meal plans.
- **HTML Output:** Produces clean, styled HTML reports for easy consumption.

***

## Getting Started

### Prerequisites

- JDK 11 or higher installed
- Java-compatible IDE or command line environment
- CSV data files (`dishes.csv` and `profiles.csv`)

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/vipul1999/HealthifyMe.git
   cd HealthifyMe
   ```

2. Place your data files:

    - `src/main/java/org/example/data/dishes.csv`
    - `src/main/java/org/example/data/profiles.csv`

3. Compile the project:

   ```bash
   javac -d out src/main/java/org/example/*.java
   ```

4. Run the application:

   ```bash
   java -cp out org.example.App
   ```

### Output

- The program generates one consolidated HTML file named `all_meal_plans.html`.
- Open this file in your web browser to view profiles alongside their personalized meal plans.

***

## Project Structure

```
src/
└─ main/
   └─ java/
      └─ org/
         └─ example/
            ├─ App.java               # Main application entrypoint
            ├─ MealPlanner.java       # Core planning and HTML output logic
            ├─ entity/
            │   ├─ Dish.java          # Dish model
            │   └─ Profile.java       # Profile model
            └─ utility/
                ├─ DishLoader.java   # CSV parser for dishes
                └─ profileLoader.java # CSV parser for profiles
```

***

## How It Works

1. **Data Loading:** Reads CSV files containing dish information and patient profiles.
2. **Filtering:** Filters dishes to exclude allergens, match diet restrictions, and meet nutritional preferences.
3. **Meal Selection:** Groups dishes by meal type and selects top scored dishes for each meal.
4. **Fallback Mechanism:** If a meal slot cannot be filled, it attempts swapping to find suitable alternatives.
5. **Snack Addition:** Adds snacks to meet calorie goals when necessary.
6. **HTML Report Generation:** Produces an HTML page per user profile with meal details, rationale, and summary.

***

## Extending the Project

- Add support for new diets or allergy types.
- Improve scoring algorithms to factor in additional nutrition metrics.
- Integrate the system with front-end UI frameworks for real-time interaction.
- Support other data sources beyond CSV (databases, APIs).

***

## Testing

- Unit tests validate filtering, scoring, and output correctness.
- Private methods tested via reflection or adjusted visibility.
- Recommended to use JUnit 5 framework for adding new tests.

***

## Contribution Guidelines

- Fork the repository.
- Create a feature branch.
- Write tests for your changes.
- Submit a detailed pull request.
- Report issues or request features through GitHub issues.

***

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) file for more details.

***

## Contact

Created and maintained by Vipul.  
GitHub: [vipul1999](https://github.com/vipul1999)

Feel free to open issues or discuss new features.

***

## Acknowledgments

Thank you to open source contributors and community libraries that made development smooth.

Inspired by real-world patient dietary management and the need for scalable, customizable meal planning.
