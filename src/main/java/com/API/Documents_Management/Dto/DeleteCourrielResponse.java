package com.API.Documents_Management.Dto;


import lombok.Builder;

@Builder
public record DeleteCourrielResponse(String courrielNumber,
                                     String courrielPath) {
}
