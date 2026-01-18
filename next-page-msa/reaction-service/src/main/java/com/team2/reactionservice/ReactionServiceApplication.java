package com.team2.reactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.team2.reactionservice",
        "com.team2.commonmodule"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
        "com.team2.reactionservice",
        "com.team2.commonmodule.feign"
})
public class ReactionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReactionServiceApplication.class, args);
    }
}
