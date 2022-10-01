# Application Monitoring Center

# 使用

## pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <properties>
        <!-- spring-boot-admin-starter-client -->
        <spring-boot-admin-starter-client.version>2.6.6</spring-boot-admin-starter-client.version>
    </properties>

    <dependencies>

        <!-- spring-boot-admin-starter-client -->
        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-client</artifactId>
            <version>${spring-boot-admin-starter-client.version}</version>
            <!--
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-actuator</artifactId>
                </exclusion>
            </exclusions>
            -->
        </dependency>
    </dependencies>
</project>
```

## application.yml

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: @artifactId@
    version: @version@

  profiles:
    active: @profiles.active@

  # spring-boot-admin-starter-client
  boot:
    admin:
      client:
        url: http://localhost:9200
        username: admin
        password: admin
        instance:
          # 默认使用的是主机名注册，改为使用ip注册
          prefer-ip: true

        actuator:
          secret: h!Fra!NUB^LRXN~Xo@%ku2?nKWunf!&U

# actuator
management:
  endpoints:
    # org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
    web:
      exposure:
        include: '*'
    # org.springframework.boot.actuate.autoconfigure.endpoint.jmx.JmxEndpointProperties
    jmx:
      exposure:
        include: '*'
  endpoint:
    health:
      enabled: true
      show-details: always
    beans:
      enabled: true
    metrics:
      enabled: true

# logging
logging:
  file:
    name: logs/debug.log

# info
info:
  name: ${spring.application.name}
  port: ${server.port}
  version: ${spring.application.version}
  active: ${spring.profiles.active}
```

## 授予 "/actuator/**" 访问权限

## ActuatorFilter.java

```java
package org.xxx.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author xiangqian
 * @date 13:36 2022/10/01
 */
//@WebFilter(filterName = "actuatorFilter", urlPatterns = "/actuator/*")
@WebFilter(filterName = "actuatorFilter", urlPatterns = "/*")
public class ActuatorFilter extends HttpFilter implements Ordered {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private WebEndpointProperties webEndpointProperties;

    @Value("${spring.boot.admin.client.actuator.secret}")
    private String secret;

    private String basePath;

    @PostConstruct
    public void _init() {
        basePath = contextPath + webEndpointProperties.getBasePath();
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
//        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(basePath)) {
            String secret = request.getHeader("actuator-secret");
            if (Objects.isNull(secret) || !this.secret.equals(secret.trim())) {
                HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
                response.setStatus(httpStatus.value());
                response.getOutputStream().println(httpStatus.getReasonPhrase());
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
```

## XxxApplication.java

```java
package org.xxx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author xiangqian
 * @date 16:08 2022/10/01
 */
@ServletComponentScan // 扫描 @WebFilter
@SpringBootApplication
public class XxxApplication {

    public static void main(String[] args) {
        SpringApplication.run(XxxApplication.class, args);
    }

}
```
