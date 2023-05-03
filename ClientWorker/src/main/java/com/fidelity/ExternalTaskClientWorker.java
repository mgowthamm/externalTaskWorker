package com.fidelity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExternalTaskClientWorker implements CommandLineRunner {
	

    @Value("${camunda.bpm.url}")
    private String camundaUrl;
    @Value("${camunda.bpm.userName}")
    private String username;
    @Value("${camunda.bpm.password}")
    private String password;
    @Value("${camunda.bpm.lockDuration}")
    private Long lockDuration;
    @Value("${camunda.bpm.timeout}")
    private Long timeout;

    
    @Override
    public void run(String... args) throws Exception {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(camundaUrl)
                .asyncResponseTimeout(timeout)
                .addInterceptor(new BasicAuthInterceptor(username, password))
                .build();

        client.subscribe("exampleTopic")
                .lockDuration(lockDuration)
                .handler((externalTask, externalTaskService) -> {
                    // Your task execution logic here
                	
                	Map <?, ?> variables = externalTask.getAllVariables();

                	System.out.println(variables);
                    externalTaskService.complete(externalTask);
                })
                .open();
    }
    
    public static class BasicAuthInterceptor implements ClientRequestInterceptor {

        private final String basicAuthHeaderValue;

        public BasicAuthInterceptor(String username, String password) {
            String credentials = username + ":" + password;
            basicAuthHeaderValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void intercept(ClientRequestContext requestContext) {
            requestContext.addHeader("Authorization", basicAuthHeaderValue);
        }
    }
}
