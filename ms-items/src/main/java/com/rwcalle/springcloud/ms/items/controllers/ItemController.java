package com.rwcalle.springcloud.ms.items.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rwcalle.libs.ms.commons.entities.Product;
import com.rwcalle.springcloud.ms.items.models.Item;
import com.rwcalle.springcloud.ms.items.services.ItemService;
//import com.rwcalle.springcloud.ms.items.services.ItemServiceFeign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;



@RefreshScope
@RestController
public class ItemController {

    private final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Value("${configuracion.texto}")
    private String text;

    @Autowired
    private Environment env;

    //itemServiceWebClient
    public ItemController(@Qualifier("itemServiceFeign") ItemService itemService, CircuitBreakerFactory circuitBreakerFactory) {
        this.itemService = itemService;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping("/fetch-configs")
    public ResponseEntity<?> fetchConfigs(@Value("${server.port}") String port) {
        Map<String, String> json = new HashMap<>();
        json.put("text", text);
        json.put("port", port);
        LOGGER.info(port);
        LOGGER.info(text);
        if(env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equals("dev")){
            json.put("autor.nombre", env.getProperty("configuracion.autor.nombre"));
            json.put("autor.email", env.getProperty("configuracion.autor.email"));
        }
        return ResponseEntity.ok(json);
    }
    

    @GetMapping
    public List<Item> list(@RequestParam(name = "name", required = false) String name,
                            @RequestHeader(name = "token-request", required = false) String tokenRequest) {
        System.out.println(name);
        System.out.println(tokenRequest);
        return itemService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id){

        Optional<Item> itemOptional = circuitBreakerFactory.create("items").run(() -> itemService.findById(id) , e -> {
            
            LOGGER.info(e.getMessage());
            
            Product product = new Product();
            product.setCreateAt(LocalDate.now());
            product.setId(1L);
            product.setName("Camara Sony xD");
            product.setPrice(500.55);
            return Optional.of(new Item(product, 5));
        });

        //Optional<Item> itemOptional = itemService.findById(id);
        if(itemOptional.isPresent()){
            return ResponseEntity.ok(itemOptional.get());
        }
        return ResponseEntity.status(404).body(Collections.singletonMap("message", "No existe el producto en ms-products"));
    }

    @CircuitBreaker(name = "items", fallbackMethod = "fallbackDetails1")
    @GetMapping("/details/{id}")
    public ResponseEntity<?> details2(@PathVariable Long id){

        Optional<Item> itemOptional = itemService.findById(id);
        if(itemOptional.isPresent()){
            return ResponseEntity.ok(itemOptional.get());
        }

        return ResponseEntity.status(404).body(Collections.singletonMap("message", "No existe el producto en ms-products"));
    }

    public ResponseEntity<?> fallbackDetails1(Long id, Throwable e){
        LOGGER.info("[details1] Fallback ejecutado. Causa: " + e.getMessage());
        Product product = new Product();
        product.setCreateAt(LocalDate.now());
        product.setId(1L);
        product.setName("Camara Sony xD - FB1");
        product.setPrice(500.55);
        return ResponseEntity.ok(new Item(product, 5));
    }

    @CircuitBreaker(name = "items", fallbackMethod = "fallbackDetails2")
    @TimeLimiter(name = "items")
    @GetMapping("/details2/{id}")
    public CompletableFuture<?> details3(@PathVariable Long id){
        return CompletableFuture.supplyAsync(() -> {
            Optional<Item> itemOptional = itemService.findById(id);
            if(itemOptional.isPresent()){
                return ResponseEntity.ok(itemOptional.get());
            }
            return ResponseEntity.status(404).body(Collections.singletonMap("message", "No existe el producto en ms-products" ));
        });
    }

    public CompletableFuture<?> fallbackDetails2(Long id, Throwable e){
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("[details2] Fallback ejecutado. Causa: " + e.getMessage());
            Product product = new Product();
            product.setCreateAt(LocalDate.now());
            product.setId(1L);
            product.setName("Camara Sony xD - FB2");
            product.setPrice(500.55);
            return ResponseEntity.ok(new Item(product, 5));
        });
    }




    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody Product product) {
        return itemService.save(product);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Product update(@RequestBody Product product, @PathVariable Long id) {
        return itemService.update(product, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        itemService.delete(id);
    }
    
}
