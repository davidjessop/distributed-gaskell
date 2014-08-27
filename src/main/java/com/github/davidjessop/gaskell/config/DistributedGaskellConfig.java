package com.github.davidjessop.gaskell.config;

import com.github.davidjessop.gaskell.AdvertisementUtil;

import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.Module;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.ModuleImplAdvertisement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

@Configuration
@ComponentScan("com.github.davidjessop.gaskell")
@EnableScheduling
@EnableWebMvc
public class DistributedGaskellConfig {

    private static final String SUBGROUP_NAME = "Make sure this is spelled the same everywhere";
    private static final String SUBGROUP_DESC = "...";
    private static final PeerGroupID SUBGROUP_ID = IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, SUBGROUP_NAME.getBytes());

    private static final String MULTICAST_NAME = "Or else you will get the wrong PipeID";


    @Autowired
    private NetworkManager manager;

    @Autowired
    private PeerGroup subgroup;

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


    @Bean
    public PeerGroup subGroup() throws Exception {

        // Launch the missiles, if you have logging on and see no exceptions
        // after this is ran, then you probably have at least the jars setup correctly.
        PeerGroup netGroup = manager.startNetwork();

        // Connect to our subgroup (all groups are subgroups of Netgroup)
        // If the group does not exist, it will be automatically created
        // Note this is suggested deprecated, not sure what the better way is
        ModuleImplAdvertisement mAdv = netGroup.getAllPurposePeerGroupImplAdvertisement();
        PeerGroup subgroup = netGroup.newGroup(SUBGROUP_ID, mAdv, SUBGROUP_NAME, SUBGROUP_DESC);

        // A simple check to see if connecting to the group worked
        if (Module.START_OK != subgroup.startApp(new String[0])) {
            throw new RuntimeException("Cannot start child peergroup");
        }

        return subgroup;
    }

    @Bean
    public PipeService pipeService() {
        return subgroup.getPipeService();
    }

    @Bean
    public DiscoveryService discoveryService() throws IOException, PeerGroupException {
        DiscoveryService discoveryService = subgroup.getDiscoveryService();
        return discoveryService;
    }

    @Bean
    public PipeID multicastId() {
        return IDFactory.newPipeID(subgroup.getPeerGroupID(), MULTICAST_NAME.getBytes());
    }

    @Bean
    @Qualifier("peerName")
    public String peerName() throws UnknownHostException {
        return System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getHostName();
    }

}
