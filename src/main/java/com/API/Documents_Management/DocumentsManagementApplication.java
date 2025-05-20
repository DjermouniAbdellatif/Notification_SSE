package com.API.Documents_Management;

import com.API.Documents_Management.Repositories.CourrielRepo;
import com.API.Documents_Management.Repositories.FileRepo;
import com.API.Documents_Management.Services.CourrielService;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@SpringBootApplication
public class DocumentsManagementApplication {

	private final CourrielService courrielService;


	public static void main(String[] args) {
		SpringApplication.run(DocumentsManagementApplication.class, args);

	}

	@Bean
	@Transactional
	public CommandLineRunner commandLineRunner(ApplicationContext ctx, CourrielService courrielService, FileRepo fileRepo, CourrielRepo courrielRepo) {
		return args -> {
			System.out.println("\n Application Started successfully ......");


		};


	}

}