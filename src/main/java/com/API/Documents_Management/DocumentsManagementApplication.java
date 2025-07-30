package com.API.Documents_Management;

import com.API.Documents_Management.Courriel.CourrielService;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@RequiredArgsConstructor
@SpringBootApplication
@EnableScheduling
public class DocumentsManagementApplication {

	private final CourrielService courrielService;


	public static void main(String[] args) {
		SpringApplication.run(DocumentsManagementApplication.class, args);

	}


}