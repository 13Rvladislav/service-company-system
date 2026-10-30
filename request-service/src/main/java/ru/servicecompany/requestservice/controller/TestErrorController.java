package ru.servicecompany.requestservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestErrorController {

    @GetMapping("/api/test/500")
    public void generateInternalServerError() {
        throw new RuntimeException("TEST HTTP 500 ERROR");
    }
}