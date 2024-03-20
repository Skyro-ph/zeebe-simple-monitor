package io.zeebe.monitor.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.text.ParseException;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public JwtAuthenticationConfigurer jwtAuthenticationConfigurer(
            @Value("${jwt.access-token-key}") String accessTokenKey
    ) throws ParseException, JOSEException {
        return new JwtAuthenticationConfigurer()
                .accessTokenStringDeserializer(new AccessTokenJwsStringDeserializer(
                        new MACVerifier(OctetSequenceKey.parse(accessTokenKey))
                ));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConfigurer jwtAuthenticationConfigurer) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers(HttpMethod.POST, "/api/processes").denyAll()
                                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                                .anyRequest().authenticated())
                .apply(jwtAuthenticationConfigurer);
        return http.build();
    }
}

//user eyJraWQiOiI5NTFiMDFjMC00OGNkLTQ0MTktODQ1OS1kYTIyOGQ5YzM1NDAiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyNTU0ZTNiZi1hZGJhLTQwMmYtOWIyYS00ZmM1MzE1ZjQ4YmUiLCJleHAiOjE3MTEzODcxMDAsImlhdCI6MTcxMDk1NTEwMCwianRpIjoiOTUxYjAxYzAtNDhjZC00NDE5LTg0NTktZGEyMjhkOWMzNTQwIiwiYXV0aG9yaXRpZXMiOlsiVVNFUiJdfQ.e9JBlrImwkNh7IHFgBDMhVB0fjGJhD3T2mYwSE2wqqs
//admin eyJraWQiOiIyNzQ0ZWJiYi0xOGFkLTRjNjMtOGE4OC05OTAwNTE0ZjRkYjkiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyNTU0ZTNiZi1hZGJhLTQwMmYtOWIyYS00ZmM1MzE1ZjQ4YmUiLCJleHAiOjE3MTEzNzY5OTgsImlhdCI6MTcxMDk0NDk5OCwianRpIjoiMjc0NGViYmItMThhZC00YzYzLThhODgtOTkwMDUxNGY0ZGI5IiwiYXV0aG9yaXRpZXMiOlsiQURNSU4iXX0.ATH3dIwilV2ka4rw03crMZ0VnIsYosbvmjq4aLcFJEM