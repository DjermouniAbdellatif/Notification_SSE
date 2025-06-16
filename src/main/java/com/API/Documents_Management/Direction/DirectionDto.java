package com.API.Documents_Management.Direction;

import lombok.Builder;

@Builder
public record DirectionDto(
        String name,
        Long divisionId,
        String divisionName
) {

}