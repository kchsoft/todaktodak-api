package com.heartsave.todaktodak_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(
		exclude = {SecurityAutoConfiguration.class}
)
public class TodaktodakApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodaktodakApiApplication.class, args);
	}

}
