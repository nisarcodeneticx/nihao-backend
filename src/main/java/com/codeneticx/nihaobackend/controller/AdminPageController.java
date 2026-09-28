package com.codeneticx.nihaobackend.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
public class AdminPageController {

    @GetMapping(value = {"/admin", "/admin.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> adminPage() {
        Path rootAdmin = Path.of(System.getProperty("user.dir"), "admin.html");
        Resource resource = rootAdmin.toFile().exists()
                ? new FileSystemResource(rootAdmin)
                : new ClassPathResource("static/admin.html");

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
}
