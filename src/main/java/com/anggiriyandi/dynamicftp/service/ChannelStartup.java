package com.anggiriyandi.dynamicftp.service;

import com.anggiriyandi.dynamicftp.constant.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Service
public class ChannelStartup {

    @Autowired
    private DynamicFtpInboundChannelResolver channelResolver;

    String configId = "123123123";

    @PostConstruct
    public void channelStart(){

        channelResolver.resolve(
                "192.168.100.3",
                "artivisi",
                "intermedia",
                "21",
                "/tmp/ftp/",
                "/home/artivisi/Documents/ftp/tes/",
                Protocol.FTP,
                configId
        );
    }

    @PreDestroy
    public void channelShutdown() throws IOException {
        channelResolver.deleteDynamicInboundConfig(configId, Protocol.FTP);
    }
}
