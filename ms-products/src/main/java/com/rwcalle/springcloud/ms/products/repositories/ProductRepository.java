package com.rwcalle.springcloud.ms.products.repositories;

import org.springframework.data.repository.CrudRepository;

import com.rwcalle.libs.ms.commons.entities.Product;

public interface ProductRepository extends CrudRepository<Product, Long>{

}
