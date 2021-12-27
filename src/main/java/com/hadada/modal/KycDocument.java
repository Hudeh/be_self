package com.hadada.modal;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "kycdocument")
public class KycDocument {
    @Id
    @GeneratedValue
    @Column(name = "kycdocumentid")
    private Long kycDocumentId;

    @Column(name = "name")
    private String name;

    @Column(name = "contenttype")
    private String contentType;

    @Column(name = "size")
    private Long size;

    @Column(name = "email")
    private String email;

    @Column(name = "status")
    private String status;

    @Lob
    private byte[] data;

    public String getStatus(){
        return (this.status == null) ? "Pending" : this.status;
    }

}
