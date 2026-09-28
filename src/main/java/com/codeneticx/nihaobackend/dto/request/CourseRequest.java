package com.codeneticx.nihaobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "HSK level is required")
    private String hskLevel;

    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;

    private String difficulty;
    private String coverImageUrl;
    private String status;
}
