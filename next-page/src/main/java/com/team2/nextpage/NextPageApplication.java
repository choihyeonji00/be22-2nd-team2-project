package com.team2.nextpage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class NextPageApplication {

  public static void main(String[] args) {
    SpringApplication.run(NextPageApplication.class, args);
  }

}
