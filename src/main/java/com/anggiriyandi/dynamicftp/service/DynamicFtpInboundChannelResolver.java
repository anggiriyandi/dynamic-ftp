package com.anggiriyandi.dynamicftp.service;


import com.anggiriyandi.dynamicftp.constant.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class DynamicFtpInboundChannelResolver {

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private ThreadPoolTaskScheduler scheduler;


    private Logger logger = LoggerFactory.getLogger(DynamicFtpInboundChannelResolver.class);
    private Map<String, MessageChannel> inboundRegistery = new HashMap<>();


    public void setInboundRegistery(LinkedHashMap<String, MessageChannel> inboundRegistery) {
        logger.info("create object baru untuk registery");
        this.inboundRegistery = inboundRegistery;
    }

    private final Map<MessageChannel, ConfigurableApplicationContext> contexts = new HashMap<>();


    public void resolve(String host, String user, String password, String port, String localDir, String remoteDir, Protocol protocol, String configurationId) {
        logger.info("INBOUND CHANNEL REGISTERY COUNT : {}", inboundRegistery.size());
        createNewFtpChannel(host, user, password, port, localDir, remoteDir, protocol, configurationId);
    }


    private synchronized MessageChannel createNewFtpChannel(String host, String user, String password, String port,
            String localDir, String remoteDir, Protocol protocol,String configurationId) {

        logger.info("CREATE NEW CONNECTION FOR CONFIGURATION ID : [{}]", configurationId);

        String xmlContext = protocol.equals(Protocol.SFTP) ? "SftpInboundChannelAdapter.xml" : "FtpInboundChannelAdapter.xml";
        String channelBean = protocol.equals(Protocol.SFTP) ? "sftpChannel" : "ftpChannel";

        MessageChannel channel = inboundRegistery.get(configurationId);
        if (channel == null) {
            logger.info("========== create new channel {} ===========", channelBean);
            logger.info("Host       : {}", host);
            logger.info("User       : {}", user);
            logger.info("Password   : {}", password);
            logger.info("Remote Dir : {}", remoteDir);
            logger.info("Local Dir  : {}", localDir);
            logger.info("============================================");

            ConfigurableApplicationContext chilsdCtx = new ClassPathXmlApplicationContext(
                    new String[]{xmlContext},
                    false);
            chilsdCtx.setId(configurationId);
            this.setEnvironmentChild(chilsdCtx, remoteDir, localDir, host, user, password, port, configurationId);
            chilsdCtx.refresh();

            channel = chilsdCtx.getBean(channelBean, MessageChannel.class);
            inboundRegistery.put(configurationId, channel);
            //Will works as the same reference is presented always
            this.contexts.put(channel, chilsdCtx);
        } else {
            logger.info("========== update channel {} ===========", channelBean);
            logger.info("Host       : {}", host);
            logger.info("User       : {}", user);
            logger.info("Password   : {}", password);
            logger.info("Remote Dir : {}", remoteDir);
            logger.info("Local Dir  : {}", localDir);
            logger.info("============================================");
            ConfigurableApplicationContext ctx = contexts.get(channel);
            if (ctx != null) { //shouldn't be null ideally
                ctx.close();
                contexts.remove(channel);

                ConfigurableApplicationContext chilsdCtx = new ClassPathXmlApplicationContext(
                        new String[]{xmlContext},
                        false);
                chilsdCtx.setId(configurationId);
                this.setEnvironmentChild(chilsdCtx, remoteDir, localDir, host, user, password, port, configurationId);
                chilsdCtx.refresh();

                channel = chilsdCtx.getBean(channelBean, MessageChannel.class);
                inboundRegistery.put(configurationId, channel);
                //Will works as the same reference is presented always
                this.contexts.put(channel, chilsdCtx);
            }
        }
        return channel;
    }


    public void deleteDynamicInboundConfig(String configurationId, Protocol protocol) throws IOException {
        String channelBean = protocol.equals(Protocol.SFTP) ? "sftpChannel" : "ftpChannel";

        //coba sshutdown threadpool
        executor.shutdown();
        scheduler.shutdown();

        MessageChannel channel = inboundRegistery.get(configurationId);
        logger.info("Reporting Ftp Config  : {}", configurationId);

        if (channel != null) {
            ConfigurableApplicationContext ctx = contexts.get(channel);
            if (ctx != null) { //shouldn't be null ideally
                logger.info("========== Deleting Dynamic Inbound channel {} ===========", channelBean);

                DefaultFtpSessionFactory defaultFtpSessionFactory = (DefaultFtpSessionFactory) ctx.getBean("ftpInboundClientFactory");

                logger.info("status koneksi ftp [{}]", defaultFtpSessionFactory.getSession().getClientInstance().isConnected());

                defaultFtpSessionFactory.getSession().getClientInstance().disconnect();
                defaultFtpSessionFactory.getSession().getClientInstance().logout();
                defaultFtpSessionFactory.getSession().getClientInstance().abort();
                defaultFtpSessionFactory.getSession().close();

                //coba stop polling
//                SourcePollingChannelAdapter pollingChannelAdapter = (SourcePollingChannelAdapter) ctx.getBean("filesInAdapter");
//                pollingChannelAdapter.stop();
//
//                MessageChannel controlBusChannel = (DirectChannel) ctx.getBean("controlChannel");
//                Message<String> operation = MessageBuilder.withPayload("'@filesInAdapter.stop()'").build();
//                controlBusChannel.send(new GenericMessage<>("'@filesInAdapter.stop()'"));
//                controlBusChannel.send(operation);

                ctx.close();
                contexts.remove(channel);
                inboundRegistery.remove(configurationId);

                logger.info("status koneksi ftp setelah disconnect [{}]", defaultFtpSessionFactory.getSession().getClientInstance().isConnected());
                logger.info("status koneksi ftp setelah abort [{}]", defaultFtpSessionFactory.getSession().getClientInstance().abort());
                logger.info("status koneksi ftp setelah logout [{}]", defaultFtpSessionFactory.getSession().getClientInstance().logout());
            }
        }
    }


    private void setEnvironmentChild(ConfigurableApplicationContext ctx,
            String remoteDir, String localDir, String host, String user, String password, String port, String configurationId) {
        StandardEnvironment env = new StandardEnvironment();
        Properties props = new Properties();
        props.setProperty("remoteDir", remoteDir);
        props.setProperty("localDir", localDir);

        props.setProperty("host", host);
        props.setProperty("port", port);
        props.setProperty("username", user);
        props.setProperty("password", password);
        props.setProperty("configId", configurationId);
        PropertiesPropertySource pps = new PropertiesPropertySource("propsChild", props);
        env.getPropertySources().addLast(pps);
        ctx.setEnvironment(env);
    }
}
