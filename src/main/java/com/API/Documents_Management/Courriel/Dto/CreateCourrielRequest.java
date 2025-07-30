package com.API.Documents_Management.Courriel.Dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;


@Builder
public record CreateCourrielRequest (
     String courrielNumber,
     String subject,
     String description,
     String courrielType,
     String nature,
     MultipartFile[] files,
     Long fromDivisionID,
     Long fromDirectionID,
     Long fromSousDirectionID,
     Long externalID,
     List<DestinationDto> destinations,
     LocalDateTime sentDate,
     LocalDateTime arrivedDate,
     LocalDateTime returnDate

){}