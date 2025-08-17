package com.API.Documents_Management.Courriel.Dto;

import com.API.Documents_Management.Direction.Direction;
import com.API.Documents_Management.Division.Division;
import com.API.Documents_Management.Entities.AlgerianMinistry;
import com.API.Documents_Management.SousDirection.SousDirection;
import lombok.Builder;

@Builder
public record StructureDto (
     String type,
     Long id,
     String name)
{



        public static StructureDto map(Division division, Direction direction, SousDirection sousDirection) {
            if (sousDirection != null) {
                return StructureDto.builder()
                        .type("SD")
                        .id(sousDirection.getId())
                        .name(sousDirection.getName())
                        .build();
            } else if (direction != null) {
                return StructureDto.builder()
                        .type("DIR")
                        .id(direction.getId())
                        .name(direction.getName())
                        .build();
            } else if (division != null) {
                return StructureDto.builder()
                        .type("DIV")
                        .id(division.getId())
                        .name(division.getName())
                        .build();
            } else {
                throw new IllegalArgumentException("Aucune structure valide fournie pour le mapping.");
            }
        }


}