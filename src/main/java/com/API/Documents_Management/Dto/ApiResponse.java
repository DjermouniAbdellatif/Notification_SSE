package com.API.Documents_Management.Dto;

import lombok.Builder;
import lombok.Data;

@Builder
public record ApiResponse<T>(boolean isSucces,String message, T data) {
}