package com.API.Documents_Management.Dto;

import com.API.Documents_Management.Enums.CourrielType;
import lombok.Builder;

@Builder
public record CourrielFilterRequest(
        String courrielNumber,
        CourrielType courrielType,
        Long fromDivisionId,
        Long toDivisionId,
        Long fromDirectionId,
        Long toDirectionId,
        Long fromSousDirectionId,
        Long toSousDirectionId,
        Long fromExternalId,
        Long toExternalId

) {}