package com.rwcalle.springcloud.ms.items;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
@SpringBootApplication
public class MsItemsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsItemsApplication.class, args);
	}

}
