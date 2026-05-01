package com.hivemind.group;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class GroupServiceApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(GroupServiceApplication.class, args);
    }
}
