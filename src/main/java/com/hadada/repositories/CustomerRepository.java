package com.hadada.repositories;

import com.hadada.modal.Customer;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    List<Customer> findAll();

    List<Customer> findByUsername(String officialEmail);

    Optional<Customer> findByAuthKey(String authKey);
}
