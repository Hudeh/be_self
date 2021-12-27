package com.hadada.modal;
import javax.persistence.*;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "callbackcount")

public class CallBackCount {
    @Id
    @GeneratedValue
    @Column(name = "callbackcountid")
    private Long callBackCountId;

    @Column(name = "appid")
    private Long appId;

    @Column(name = "callcount")
    private Integer callCount;

    @Column(name = "callbackdate")
    private String callBackDate;

    @Column(name = "sessionkey")
    private String sessionKey;
}
