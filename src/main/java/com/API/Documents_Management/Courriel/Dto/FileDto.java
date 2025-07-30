package com.API.Documents_Management.Courriel.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder

public record FileDto (
    String fileName,
    String filePath,
    String fileType,
    String fileSize ){
}