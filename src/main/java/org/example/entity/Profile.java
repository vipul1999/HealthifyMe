package org.example.entity;

import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {
    private int calorieTarget;
    private Set<String> excludeAllergens;
    private String diet; // vegetarian, vegan, omnivore
    private Map<String, Boolean> preferences;
    private int age;
    private Set<String> preferredRegions;
}
