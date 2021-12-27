package com.hadada.repositories;


import com.hadada.modal.CallBackCount;
import com.hadada.modal.App;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface CallBackCountRepository extends CrudRepository<CallBackCount, Long> {
    List<CallBackCount> findByAppIdIn(Collection<Long> appIds);
    List<CallBackCount> findBySessionKeyIn(Collection<String> sessionKeys);
}
