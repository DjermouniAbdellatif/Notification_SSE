package com.API.Documents_Management.Courriel.Dto;

import lombok.Builder;

@Builder
public record DestinationDto (
     Long toDivisionID,
     Long toDirectionID,
     Long toSousDirectionID,
     Long toExternalID
){};