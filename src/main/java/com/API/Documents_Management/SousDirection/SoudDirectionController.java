package com.API.Documents_Management.SousDirection;

import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Direction.DirectionDto;
import com.API.Documents_Management.Direction.DirectionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class SoudDirectionController {

    private final SousDierctionRepo sousDierctionRepo;

    @GetMapping("/sous_direction/all")
    public List<SousDirectionDto> getAllSousDirections() {
        List<SousDirection> sousDirections = sousDierctionRepo.findAll();

        return sousDirections.stream()
                .map(sd -> SousDirectionDto.builder()
                        .name(sd.getName())
                        .directionId(sd.getDirection().getId())
                        .directionName(sd.getDirection().getName())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
