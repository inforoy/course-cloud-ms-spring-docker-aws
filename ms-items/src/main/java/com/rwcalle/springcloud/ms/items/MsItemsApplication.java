package com.rwcalle.springcloud.ms.items;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MsItemsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsItemsApplication.class, args);
	}

}
