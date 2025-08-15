package com.spring.springbootapplication.config;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.spring.springbootapplication.service.MyUserDetailsService;

import jakarta.servlet.DispatcherType;

@Configuration
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider(
        MyUserDetailsService uds,   // ★ 型で特定
        PasswordEncoder encoder) {
    var p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(encoder);
    return p;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider dao) throws Exception {
        http.authenticationProvider(dao)

            // 認証の設定: 記載したURLパターンを許可
            .authorizeHttpRequests(auth -> auth
            .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
            .requestMatchers("/css/**","/js/**","/images/**","/uploads/**","/error","/error/**","/debug/**").permitAll()
            .requestMatchers("/login", "/register").permitAll()
            .anyRequest().authenticated()  // それ以外は認証が必要
            )

            // フォームログインの設定
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")  // 処理（明示）
                .usernameParameter("email")  
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true)  // ログイン成功時の遷移先
                .failureUrl("/login?error=true")  // 失敗時のURL
                .permitAll()  // ログインページは誰でもアクセス可能
            )
            // ログアウトの設定
            .logout(logout -> logout
                .logoutUrl("/logout")           
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)  // セッションの無効化
                .deleteCookies("JSESSIONID")  // Cookie削除
                .permitAll()  // ログアウトは誰でも実行可能
            )  

            // セッション管理
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/login", "/register"));

        return http.build();
    }
}
