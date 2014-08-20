package com.github.davidjessop.gaskell.config;

import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.IOException;
import java.util.Random;

@Configuration
@ComponentScan("com.github.davidjessop.gaskell")
@EnableScheduling
public class DistributedGaskellConfig {

    @Bean
    public NetworkManager networkManager() throws IOException {
        // Add a random number to make it easier to identify by name, will also make sure the ID is unique
        String peerName = "Peer " + new Random().nextInt(1000000);

        // Here the local peer cache will be saved, if you have multiple peers this must be unique
        File conf = new File("." + System.getProperty("file.separator") + peerName);

        NetworkManager networkManager = new NetworkManager(NetworkManager.ConfigMode.ADHOC, peerName, conf.toURI());

        // This is what you will be looking for in Wireshark instead of an IP, hint: filter by "jxta"
        PeerID peer_id = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID, peerName.getBytes());
        // Randomize a port to use with a number over 1000 (for non root on unix)
        // JXTA uses TCP for incoming connections which will conflict if more than
        // one Hello runs at the same time on one computer.
        int port = 9000 + new Random().nextInt(100);

        NetworkConfigurator configurator = networkManager.getConfigurator();
        configurator.setTcpPort(port);
        configurator.setTcpEnabled(true);
        configurator.setTcpIncoming(true);
        configurator.setTcpOutgoing(true);
        configurator.setUseMulticast(true);
        configurator.setPeerID(peer_id);

        return networkManager;
    }



}
