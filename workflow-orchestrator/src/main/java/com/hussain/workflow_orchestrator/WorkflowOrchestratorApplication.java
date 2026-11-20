package com.hussain.workflow_orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ConfigurationPropertiesScan("com.hussain.workflow_orchestrator.config")
@EnableScheduling
public class WorkflowOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowOrchestratorApplication.class, args);
    }
}