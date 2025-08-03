package com.harsh.AppointDoctor.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@org.springframework.context.annotation.Configuration
@ComponentScan(basePackages = "com.harsh.AppointDoctor")
public class SocketIOConfig {

    @Value("${socket.host:localhost}")
    private String host;

    @Value("${socket.port:8081}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);

        // CORS configuration
        config.setOrigin("*");
        config.setAllowCustomRequests(true);

        // Connection configuration
        config.setMaxFramePayloadLength(1024 * 1024);
        config.setMaxHttpContentLength(1024 * 1024);

        return new SocketIOServer(config);
    }


    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }
}

