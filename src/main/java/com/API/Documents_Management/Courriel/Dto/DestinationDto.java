package com.API.Documents_Management.Courriel.Dto;

import lombok.Builder;

@Builder
public record DestinationDto (
     Long divisionID,
     Long directionID,
     Long sousDirectionID,
     Long externalID
){};