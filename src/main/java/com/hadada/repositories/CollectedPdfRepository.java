package com.hadada.repositories;


import com.hadada.modal.CollectedPDF;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface CollectedPdfRepository extends CrudRepository<CollectedPDF, Long> {
    @Transactional
    List<CollectedPDF> findBySessionKeyIn(List<String> sessionKey);
}
