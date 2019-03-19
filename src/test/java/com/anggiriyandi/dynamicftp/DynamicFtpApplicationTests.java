package com.anggiriyandi.dynamicftp;

import com.anggiriyandi.dynamicftp.constant.Protocol;
import com.anggiriyandi.dynamicftp.service.DynamicFtpInboundChannelResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DynamicFtpApplicationTests {

	@Autowired
	private DynamicFtpInboundChannelResolver channelResolver;

	@Test
	public void contextLoads() throws IOException, InterruptedException {

		String configId = "123123123";

		channelResolver.resolve(
				"192.168.100.3",
				"artivisi",
				"intermedia",
				"21",
				"/tmp/ftp/",
				"/home/artivisi/Documents/ftp/tes",
				Protocol.FTP,
				configId
		);

		Thread.sleep(30000);

		channelResolver.deleteDynamicInboundConfig(configId, Protocol.FTP);

	}

}
