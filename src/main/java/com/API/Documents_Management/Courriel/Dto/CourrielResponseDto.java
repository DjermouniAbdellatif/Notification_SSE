package com.API.Documents_Management.Courriel.Dto;

import com.API.Documents_Management.Courriel.Enums.CourrielType;
import com.API.Documents_Management.Courriel.Enums.NatureCourriel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder

public record CourrielResponseDto (
    String courrielNumber,
     String subject,
     String description,

     LocalDateTime sentDate,
     LocalDateTime arrivedDate,
     LocalDateTime returnDate,
     LocalDateTime saveDate,

     CourrielType courrielType,
     NatureCourriel nature,

     StructureDto from,
     List<StructureDto> destinations,

     List<FileDto> files       ){



}