package com.API.Documents_Management.Division;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DivisionController {

    private final DivisionRepo divisionRepo;

    @GetMapping("/division/all")
    public List<Division> getDivisions() {

        return divisionRepo.findAll();
    }
}
