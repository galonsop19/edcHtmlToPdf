package com.credix.edcHtmlToPdf;

import org.jsondoc.spring.boot.starter.EnableJSONDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableJSONDoc
@EnableAsync


public class edcHtmlToPdfApplication {

    public static void main(String[] args) {
        SpringApplication.run(edcHtmlToPdfApplication.class, args);
        Process process = new Process();
        process.printProcess();
    }
}
