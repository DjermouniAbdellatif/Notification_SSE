package com.API.Documents_Management.Dto;

import lombok.Builder;

@Builder
public record SkippedFileError(String fileName, String reason) {}
