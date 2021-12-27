package com.hadada.services;

import com.hadada.modal.FormData;
import com.hadada.modal.KycDocument;
import com.hadada.repositories.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;

@Service
public class FileService {

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void save(FormData formData) throws IOException {
        if(formData.getKycDocument() != null) {
            saveFile(formData.getKycDocument(), formData.getEmail());
        }
        if(formData.getKycDocument1() != null) {
            saveFile(formData.getKycDocument1(), formData.getEmail());
        }
        if(formData.getKycDocument2() != null) {
            saveFile(formData.getKycDocument2(), formData.getEmail());
        }
        if(formData.getKycDocument3() != null) {
            saveFile(formData.getKycDocument3(), formData.getEmail());
        }
    }
    public void saveFile(MultipartFile file, String email) throws IOException{
        KycDocument fileEntity = new KycDocument();
        fileEntity.setName(StringUtils.cleanPath(file.getOriginalFilename()));
        fileEntity.setContentType(file.getContentType());
        fileEntity.setData(file.getBytes());
        fileEntity.setSize(file.getSize());
        fileEntity.setEmail(email);
        fileRepository.save(fileEntity);
    }

    @Transactional
    public void updateFile(MultipartFile file, Long kycDocumentId) throws IOException{
        KycDocument kycDocument = fileRepository.findByKycDocumentId(kycDocumentId);
        kycDocument.setData(file.getBytes());
        kycDocument.setContentType(file.getContentType());
        kycDocument.setName(StringUtils.cleanPath(file.getOriginalFilename()));
        kycDocument.setSize(file.getSize());
        fileRepository.save(kycDocument);
    }

    @Transactional
    public void deleteFile(Long id) {
        fileRepository.deleteByKycDocumentId(id);
    }

    @Transactional
    public KycDocument getFile(Long id) {
        return fileRepository.findByKycDocumentId(id);
    }

    @Transactional
    public List<KycDocument> getAllFiles(String email) {
        return fileRepository.findByEmail(email);
    }
}