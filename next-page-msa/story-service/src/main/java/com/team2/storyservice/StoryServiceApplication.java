package com.team2.storyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.team2.storyservice",
        "com.team2.commonmodule"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
        "com.team2.storyservice",
        "com.team2.commonmodule.feign"
})
public class StoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StoryServiceApplication.class, args);
    }
}
