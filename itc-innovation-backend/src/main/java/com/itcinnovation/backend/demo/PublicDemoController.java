package com.itcinnovation.backend.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/demo/requests")
public class PublicDemoController {

    private final DemoRequestService demoRequestService;

    public PublicDemoController(DemoRequestService demoRequestService) {
        this.demoRequestService = demoRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemoCredentialsResponse requestDemo(@Valid @RequestBody DemoRequestForm form) {
        return demoRequestService.create(form);
    }
}