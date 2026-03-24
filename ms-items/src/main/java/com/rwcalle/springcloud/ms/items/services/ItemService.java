package com.rwcalle.springcloud.ms.items.services;

import java.util.List;
import java.util.Optional;

import com.rwcalle.libs.ms.commons.entities.Product;
import com.rwcalle.springcloud.ms.items.models.Item;

public interface ItemService {

    List<Item> findAll();
    Optional<Item> findById(Long id); 

    Product save(Product product);
    Product update(Product product, Long id);
    void delete(Long id);

}
