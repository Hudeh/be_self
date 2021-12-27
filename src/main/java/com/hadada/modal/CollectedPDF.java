package com.hadada.modal;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "collectedpdf")
public class CollectedPDF {
    @Id
    @GeneratedValue
    @Column(name = "collectedpdfid")
    private Long collectedPdfId;

    @Column(name = "customername")
    private String customerName;

    @Lob
    @Column(name = "pdfdata")
    private String pdfData;

    @Column(name = "sessionkey")
    private String sessionKey;

    @Column(name = "collecteddate")
    private String collectedDate;
}
