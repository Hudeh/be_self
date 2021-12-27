package com.hadada.repositories;

import com.hadada.modal.App;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface AppRepository extends CrudRepository<App, Long> {
    List<App> findAll();
    List<App> findByEmail(String email);
    List<App> findByEmailAndEnvironment(String email, String environment);
    App findByAppKey(String appKey);
    Optional<App> findByAppMigratedId(Long appId);

}
