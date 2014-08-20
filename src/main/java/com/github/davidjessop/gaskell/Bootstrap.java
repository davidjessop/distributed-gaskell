package com.github.davidjessop.gaskell;

import com.github.davidjessop.gaskell.config.DistributedGaskellConfig;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Bootstrap {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(DistributedGaskellConfig.class);

    }

}
