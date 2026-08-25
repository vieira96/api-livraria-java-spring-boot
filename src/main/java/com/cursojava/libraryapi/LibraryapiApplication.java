package com.cursojava.libraryapi;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class LibraryapiApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(LibraryapiApplication.class);
		builder.bannerMode(Banner.Mode.OFF);
		builder.run(args);
	}

}
