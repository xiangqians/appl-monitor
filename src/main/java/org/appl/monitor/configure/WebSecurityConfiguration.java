package org.appl.monitor.configure;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.function.Supplier;

/**
 * @author xiangqian
 * @date 12:34 2022/10/01
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AdminServerProperties adminServerProperties;

    @Value("${spring.boot.admin.client.actuator.secret}")
    private String secret;

    /**
     * https://github.com/codecentric/spring-boot-admin/issues/1253
     *
     * @return
     */
    @Bean
    public HttpHeadersProvider httpHeadersProvider() {
        return instance -> {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("actuator-secret", secret);
            return httpHeaders;
        };
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        Supplier<AuthenticationSuccessHandler> authenticationSuccessHandlerSupplier = () -> {
            SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
            successHandler.setTargetUrlParameter("redirectTo");
            successHandler.setDefaultTargetUrl(adminServerProperties.getContextPath() + "/");
            return successHandler;
        };

        http.authorizeRequests()
                // ?????????????????????????????????
                .antMatchers("/assets/**").permitAll()
                // ??????????????????????????????
                .antMatchers("/login").permitAll()
                // ???????????????????????????????????????
                .anyRequest().authenticated()
                .and()
                // ??????????????????
                .formLogin().loginPage("/login")
                .successHandler(authenticationSuccessHandlerSupplier.get())
                .and()
                // ????????????
                .logout().logoutUrl("/logout")
                .and()

                .httpBasic()
                .and()

                // csrf
                .csrf()
                // ??????Cookies??????CSRF??????
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // ????????????????????????CSRF-Protection
                .ignoringAntMatchers("/instances", "/actuator/**");
    }

}
