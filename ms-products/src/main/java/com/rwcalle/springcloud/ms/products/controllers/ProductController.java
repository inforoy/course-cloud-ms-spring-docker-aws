package com.rwcalle.springcloud.ms.products.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.rwcalle.springcloud.ms.products.entities.Product;
import com.rwcalle.springcloud.ms.products.services.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class ProductController {

    final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> listProducts() {
        return ResponseEntity.ok(productService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getDetails(@PathVariable Long id) throws InterruptedException {
        
        /* INICIO - CODIGO DE PRUEBAS */
        if(id.equals(10L)){
            throw new IllegalStateException("Producto no encontrado");
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
    


}
