package com.iapex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableAsync
public class IapexApiRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(IapexApiRestApplication.class, args);
	}

	@Bean
	WebMvcConfigurer corsConfigurer(){
		return new WebMvcConfigurer(){
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
				.allowedOrigins("http://localhost:4200", "http://localhost:8100")
				.allowedMethods("*")
						.allowedHeaders("*");
			}
		};
	}
}