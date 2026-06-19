package com.employeehub.employeehub;

import com.employeehub.employeehub.config.JwtProperties;
import com.employeehub.employeehub.config.S3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, S3Properties.class})
public class EmployeeHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeHubApplication.class, args);
	}

}
