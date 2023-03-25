package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestaCtrl {
    private final TestaClient testaClient;

    @Autowired
    public TestaCtrl(TestaClient testaClient) {
        this.testaClient = testaClient;
    }

    @GetMapping("/a")
    public String a() {
        return testaClient.a();
    }
}
