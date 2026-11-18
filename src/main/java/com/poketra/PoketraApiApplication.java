package com.poketra;

import com.poketra.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class PoketraApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoketraApiApplication.class, args);
    }

}
