package org.appl.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xiangqian
 * @date 12:17 2022/10/01
 */
@EnableAdminServer
@SpringBootApplication
public class ApplMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplMonitorApplication.class, args);
    }

}
