package com.hazelcast.guide.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FlushMode;
import org.springframework.session.MapSession;
import org.springframework.session.SaveMode;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.hazelcast.Hazelcast4IndexedSessionRepository;
import org.springframework.session.hazelcast.Hazelcast4PrincipalNameExtractor;
import org.springframework.session.hazelcast.HazelcastSessionSerializer;
import org.springframework.session.hazelcast.config.annotation.SpringSessionHazelcastInstance;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

import java.util.Collections;

@Configuration
@EnableHazelcastHttpSession
class SessionConfiguration {

    private final String SESSIONS_MAP_NAME = "spring-session-map-name";
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionConfiguration.class);

    @Bean
    public SessionRepositoryCustomizer<Hazelcast4IndexedSessionRepository> customize() {
        return (sessionRepository) -> {
            sessionRepository.setFlushMode(FlushMode.IMMEDIATE);
            sessionRepository.setSaveMode(SaveMode.ALWAYS);
            sessionRepository.setSessionMapName(SESSIONS_MAP_NAME);
            sessionRepository.setDefaultMaxInactiveInterval(900);
        };
    }

    @Bean("custom-hazelcast")
    @SpringSessionHazelcastInstance
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.getNetworkConfig().setPort(5710);
        config.getNetworkConfig().setPortAutoIncrement(true);
        config.setClusterName("spring-session-cluster");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setMembers(Collections.singletonList("127.0.0.1"));
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);

        // Add this attribute to be able to query sessions by their PRINCIPAL_NAME_ATTRIBUTE's
        AttributeConfig attributeConfig = new AttributeConfig()
                .setName(Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
                .setExtractorClassName(Hazelcast4PrincipalNameExtractor.class.getName());

        // Configure the sessions map
        config.getMapConfig(SESSIONS_MAP_NAME)
                .addAttributeConfig(attributeConfig).addIndexConfig(
                        new IndexConfig(IndexType.HASH, Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE));

        // Use custom serializer to de/serialize sessions faster. This is optional.
        // Note that, all members in a cluster and connected clients need to use the
        // same serializer for sessions. For instance, clients cannot use this serializer
        // where members are not configured to do so.
        SerializerConfig serializerConfig = new SerializerConfig();
        serializerConfig.setImplementation(new HazelcastSessionSerializer()).setTypeClass(MapSession.class);
        config.getSerializationConfig().addSerializerConfig(serializerConfig);

        return Hazelcast.newHazelcastInstance(config);
    }

/*  Hazelcast listener for client connection
    private HazelcastInstance configurationLog(HazelcastInstance instance) {
        instance.getClientService().addClientListener(new ClientListener() {
            @Override
            public void clientConnected(Client client) {
                LOGGER.info("Client : {} is connected to member: {}", client.getName(), instance.getName());
                LOGGER.info("Client : {} is connected with labels {}", client.getName(), client.getLabels());
            }

            @Override
            public void clientDisconnected(Client client) {

            }
        });
        return instance;
    }*/

    /* Hazelcast Client Instance Bean
    @Bean
    @SpringSessionHazelcastInstance
    public HazelcastInstance hazelcastInstance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701");
        clientConfig.getUserCodeDeploymentConfig().setEnabled(true).addClass(Session.class)
                .addClass(MapSession.class).addClass(Hazelcast4SessionUpdateEntryProcessor.class);
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
    */

}
