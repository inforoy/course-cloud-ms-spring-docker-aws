package com.rwcalle.springcloud.ms.items;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
//import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    //@Value("${config.baseurl.endpoint.ms-products}")
    @Value("${config.baseurl.endpoint.ms_products}")
    private String url;

    /*
    /@Bean
    @LoadBalanced
    WebClient.Builder webClient(){
        return WebClient.builder().baseUrl(url);
    }
    */

    @Bean
    WebClient webClient(
        //WebClient.Builder webClientBuilder,
        ReactorLoadBalancerExchangeFilterFunction lbFunction){
        return WebClient.builder().baseUrl(url).filter(lbFunction).build();
    }

}
