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

    @GetMapping(value = {"/", "/index.html", "/admin"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> adminPage() {
        Path rootPage = Path.of(System.getProperty("user.dir"), "index.html");
        Resource resource = rootPage.toFile().exists()
                ? new FileSystemResource(rootPage)
                : new ClassPathResource("static/index.html");

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
}
