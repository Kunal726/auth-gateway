package com.projects.marketmosaic.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping(path = "/api/data")
    ResponseEntity<String> getResp(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok("Hello");
    }
}
