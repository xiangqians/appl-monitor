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
                // 授予静态资产和访问权限
                .antMatchers("/assets/**").permitAll()
                // 授予登录页面访问权限
                .antMatchers("/login").permitAll()
                // 其他所有请求都必须经过验证
                .anyRequest().authenticated()
                .and()
                // 表单登陆配置
                .formLogin().loginPage("/login")
                .successHandler(authenticationSuccessHandlerSupplier.get())
                .and()
                // 登出配置
                .logout().logoutUrl("/logout")
                .and()

                .httpBasic()
                .and()

                // csrf
                .csrf()
                // 使用Cookies启用CSRF保护
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // 对执行器端点禁用CSRF-Protection
                .ignoringAntMatchers("/instances", "/actuator/**");
    }

}
