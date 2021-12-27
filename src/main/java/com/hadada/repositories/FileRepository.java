package com.hadada.repositories;

import com.hadada.modal.App;
import com.hadada.modal.KycDocument;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends CrudRepository<KycDocument, String> {
    List<KycDocument> findByEmail(String email);
    KycDocument findByKycDocumentId(Long kycDocumentId);
    void deleteByKycDocumentId(Long kycDocumentId);
}