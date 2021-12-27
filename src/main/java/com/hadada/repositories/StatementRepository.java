package com.hadada.repositories;

import com.hadada.modal.App;
import com.hadada.modal.Statement;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StatementRepository extends CrudRepository<Statement, Long> {
    List<Statement> findByEmail(String email);
}
