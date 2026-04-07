package com.rwcalle.springcloud.app.gateway.ms_gateway_server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest
class MsGatewayServerApplicationTests {

	@MockitoBean
	JwtDecoder jwtDecoder;

	@MockitoBean
	ClientRegistrationRepository clientRegistrationRepository;

	@Test
	void contextLoads() {

	}

}
