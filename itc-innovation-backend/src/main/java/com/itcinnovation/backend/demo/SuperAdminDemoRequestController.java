package com.itcinnovation.backend.demo;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/super-admin/demo-requests")
public class SuperAdminDemoRequestController {

    private final DemoRequestService demoRequestService;

    public SuperAdminDemoRequestController(DemoRequestService demoRequestService) {
        this.demoRequestService = demoRequestService;
    }

    @GetMapping
    public List<DemoRequestResponse> listRequests() {
        return demoRequestService.listRequests();
    }
}