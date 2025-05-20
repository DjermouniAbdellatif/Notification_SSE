package com.API.Documents_Management.Dto;


import lombok.Builder;

@Builder
public record DeleteFileResponse(String courrielNumber,
                                 String courrielPath,
                                 String fileName) {
}
