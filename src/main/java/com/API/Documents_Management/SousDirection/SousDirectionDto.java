package com.API.Documents_Management.SousDirection;

import lombok.Builder;

@Builder
public record SousDirectionDto(
        String name,
        Long directionId,
        String directionName
) {

}