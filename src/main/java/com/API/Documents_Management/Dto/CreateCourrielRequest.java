package com.API.Documents_Management.Dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Builder
public record CreateCourrielRequest(String courrielNumber, List<MultipartFile> files) {}
