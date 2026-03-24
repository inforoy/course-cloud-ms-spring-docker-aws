package com.rwcalle.libs.ms.commons;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class LibsMsCommonsApplication {

	public static void main(String[] args) {
		//SpringApplication.run(LibsMsCommonsApplication.class, args);
	}

}
