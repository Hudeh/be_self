package com.hadada.modal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class FormData {
    MultipartFile kycDocument;
    MultipartFile kycDocument1;
    MultipartFile kycDocument2;
    MultipartFile kycDocument3;
    String email;
}
