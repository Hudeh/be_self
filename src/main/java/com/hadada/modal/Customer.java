package com.hadada.modal;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue
    @Column(name = "customerid")
    private Long customerId ;

    @Column(name = "username")
    private String username ;

    @Column(name = "password ")
    private String password ;

    @Column(name = "authkey")
    private String authKey ;

    @Column(name = "rolekey")
    private Long roleKey ;

    @Column(name = "pin")
    private String pin;

    @Column(name = "clientid")
    private String clientId;

    @Column(name = "LogoUrl")
    private String logoUrl;

    @Column(name = "BrandName")
    private String brandName;

    @Transient
    private String status;

    @Transient
    private String token;

    @Column(name = "wallet")
    private Long wallet ;
}
