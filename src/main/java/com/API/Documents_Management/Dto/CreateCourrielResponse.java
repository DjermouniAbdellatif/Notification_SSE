package com.API.Documents_Management.Dto;


import lombok.Builder;

import java.util.List;

@Builder
public record CreateCourrielResponse(
        String courrielNumber,
        List<UploadFileResponse> uploadedFiles,
        List<SkippedFileError> skippedFiles
) {}