package com.rwcalle.springcloud.ms.products;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.rwcalle.libs.ms.commons.entities"})
public class MsProductsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsProductsApplication.class, args);
	}

}
