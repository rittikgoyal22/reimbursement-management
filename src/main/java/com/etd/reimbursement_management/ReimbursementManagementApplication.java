package com.etd.reimbursement_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReimbursementManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReimbursementManagementApplication.class, args);
	}

}
