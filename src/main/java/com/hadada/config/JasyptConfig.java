package com.hadada.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//spring annotation
@Configuration
public class JasyptConfig {

    static String password = "hadadaselfservice";

    static String algorithm = "PBEWithMD5AndDES";

    //common method
    //used in classes - JasyptConfig.java and EncryptDecryptPwd.java
    public static SimpleStringPBEConfig getSimpleStringPBEConfig() {
        final SimpleStringPBEConfig pbeConfig = new SimpleStringPBEConfig();
        pbeConfig.setPassword(password);  //encryptor private key
        pbeConfig.setAlgorithm(algorithm);
        pbeConfig.setKeyObtentionIterations("1000");
        pbeConfig.setPoolSize("1");
        pbeConfig.setProviderName("SunJCE");
        pbeConfig.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        pbeConfig.setStringOutputType("base64");

        return pbeConfig;
    }

    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor encryptor() {
        final PooledPBEStringEncryptor pbeStringEncryptor = new PooledPBEStringEncryptor();
        pbeStringEncryptor.setConfig(getSimpleStringPBEConfig());

        return pbeStringEncryptor;
    }
}
