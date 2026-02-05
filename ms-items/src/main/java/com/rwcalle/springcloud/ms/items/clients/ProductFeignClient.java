package com.rwcalle.springcloud.ms.items.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.rwcalle.springcloud.ms.items.models.Product;

@FeignClient(name = "ms-products")
public interface ProductFeignClient {

    @GetMapping
    List<Product> findAll();

    @GetMapping("/{id}")
    Product details(@PathVariable Long id);

}
