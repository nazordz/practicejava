package com.nazor.practicejava;

import org.springframework.boot.SpringApplication;

public class TestPracticejavaApplication {

	public static void main(String[] args) {
		SpringApplication.from(PracticejavaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
