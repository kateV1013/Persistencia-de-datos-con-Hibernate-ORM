package com.quilate.api;

import com.quilate.util.JPAUtil;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuilateApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuilateApiApplication.class, args);
    }

    @PreDestroy
    public void cerrar() {
        JPAUtil.cerrar();
    }
}
