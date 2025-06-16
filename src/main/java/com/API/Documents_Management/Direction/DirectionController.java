package com.API.Documents_Management.Direction;

import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Division.DivisionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class DirectionController {

    private final DirectionRepo directionRepo;

    @GetMapping("/direction/all")
    public List<DirectionDto> getAllDirections() {
        List<Direction> directions = directionRepo.findAll();

        return directions.stream()
                .map(direction -> DirectionDto.builder()
                        .name(direction.getName())
                        .divisionId(direction.getDivision().getId())
                        .divisionName(direction.getDivision().getName())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
