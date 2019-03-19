/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.anggiriyandi.dynamicftp.service;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.Message;

import java.io.File;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author anggi
 */

public class DynamicFtpInboundServiceActivator {

    //config id yg akan di terima
    private  @Setter String configId;
    private Logger logger = LoggerFactory.getLogger(DynamicFtpInboundServiceActivator.class);

    @ServiceActivator
    public void process(Message<File> message) {

        File file = message.getHeaders().get(FileHeaders.ORIGINAL_FILE, File.class);

        logger.info("======== masuk ke processing handler ===========");
        logger.info("hasil input ftp : {} - {}", message.getHeaders(), message.getPayload());
        logger.info("Config id yang di dapat : [{}]", configId);

        //logic disini

    }
}
