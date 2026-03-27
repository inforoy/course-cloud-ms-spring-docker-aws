package com.rwcalle.springcloud.ms.items.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

//import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import com.rwcalle.libs.ms.commons.entities.Product;
import com.rwcalle.springcloud.ms.items.models.Item;

//@Primary
@Service
public class ItemServiceWebClient implements ItemService {

    private final WebClient.Builder client;

    public ItemServiceWebClient(Builder client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        
        return this.client.build()
            .get()
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(Product.class)
            .map(product -> new Item(product, new Random().nextInt(10) + 1))
            .collectList()
            .block();

    }

    @Override
    public Optional<Item> findById(Long id) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        //try {
            return Optional.of(client.build().get().uri("/{id}", params)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(Product.class)
                            .map(product -> new Item(product, new Random().nextInt(10) + 1))
                            .block());
        //} catch (WebClientException e) {
        //    return Optional.empty();
        //}
        
    }

    @Override
    public Product save(Product product) {
        return client.build()
        .post()
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(product)
        .retrieve()
        .bodyToMono(Product.class)
        .block();
    }

    @Override
    public Product update(Product product, Long id) {
        Map<String, Long> params = new HashMap<>();
        params.put("id", id);

        return client.build()
            .put()
            .uri("/{id}", params)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(product)
            .retrieve()
            .bodyToMono(Product.class)
            .block();
    }

    @Override
    public void delete(Long id) {
        Map<String, Long> params = new HashMap<>();
        params.put("id", id);

        client.build()
            .delete()
            .uri("/{id}", params)
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }



}
