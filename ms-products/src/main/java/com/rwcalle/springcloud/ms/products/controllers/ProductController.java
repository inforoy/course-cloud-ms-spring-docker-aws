package com.rwcalle.springcloud.ms.products.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.rwcalle.libs.ms.commons.entities.Product;
import com.rwcalle.springcloud.ms.products.services.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
public class ProductController {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    //public ResponseEntity<List<Product>> listProducts() {
    public ResponseEntity<List<Product>> listProducts(@RequestHeader(name = "message-request", required = false) String messageRequest) {
        LOGGER.info("Llamada al controlador ProductController::listProducts()");
        LOGGER.info("Message : {}", messageRequest);
        return ResponseEntity.ok(productService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getDetails(@PathVariable Long id) throws InterruptedException {
        
        /* INICIO - CODIGO DE PRUEBAS */
        if(id.equals(10L)){
            //throw new IllegalStateException("Producto no encontrado");
        }

        if(id.equals(7L)){
            TimeUnit.SECONDS.sleep(3L);
        }
        /* FIN - CODIGO DE PRUEBAS */

        Optional<Product> productOptional = productService.findById(id);
        if(productOptional.isPresent()){
            return ResponseEntity.ok(productOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        LOGGER.info("Llamada al controlador ProductController::create(), creando producto: {}", product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Product product) {
        LOGGER.info("Llamada al controlador ProductController::update(), actualizando producto: {}", product);
        Optional<Product> productOptional = productService.findById(id);
        if(productOptional.isPresent()){
            Product productDB = productOptional.orElseThrow();
            productDB.setName(product.getName());
            productDB.setPrice(product.getPrice());
            productDB.setCreateAt(product.getCreateAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(productDB));
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        LOGGER.info("Llamada al controlador ProductController::delete(), eliminando producto con id: {}", id);
        Optional<Product> productOptional = productService.findById(id);
        if(productOptional.isPresent()){
            this.productService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();

    }

}
