package com.API.Documents_Management.Dto;


import lombok.Builder;

@Builder
public record UploadFileResponse(
        String fileName,
        String filePath,
        String fileSize // MB
) {}