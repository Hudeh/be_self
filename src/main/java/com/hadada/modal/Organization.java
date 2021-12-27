package com.hadada.modal;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "organization")
public class Organization {
    @Id
    @GeneratedValue
    @Column(name = "organizationkey")
    private Long organizationKey;

    @Column(name = "organizationname")
    private String organizationName;

    @Column(name = "organizationcountry")
    private String organizationCountry;

    @Column(name = "employeename")
    private String employeeName;

    @Column(name = "officialemail")
    private String officialEmail;

    @Column(name = "phonenumber")
    private String phoneNumber;

    @Column(name = "clientid")
    private String clientId;

    @Column(name = "status")
    private String status;

}
