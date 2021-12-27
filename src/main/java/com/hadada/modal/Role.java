package com.hadada.modal;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue
    @Column(name = "rolekey")
    private Long roleKey;

    @Column(name = "roleName")
    private String roleName;
}
