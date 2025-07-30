package com.API.Documents_Management.Courriel.Dto;

import com.API.Documents_Management.Courriel.Courriel;
import com.API.Documents_Management.Courriel.CourrielType;
import com.API.Documents_Management.Courriel.NatureCourriel;
import com.API.Documents_Management.Utils.FormatUtils;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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