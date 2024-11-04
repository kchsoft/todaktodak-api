package com.heartsave.todaktodak_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TodaktodakApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(TodaktodakApiApplication.class, args);
  }
}
