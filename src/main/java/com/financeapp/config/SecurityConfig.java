package com.financeapp.config;

import com.financeapp.repository.UserRepository;
import com.financeapp.security.jwt.JwtAuthenticationEntryPoint;
import com.financeapp.security.jwt.JwtAuthenticationFilter;
import com.financeapp.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * Key decisions:
 *  - Stateless sessions (STATELESS) — JWT carries all auth state.
 *  - CSRF disabled — REST APIs use tokens, not cookies.
 *  - Role-based URL authorization combined with method-level @PreAuthorize.
 *  - BCrypt for password hashing (work factor 12).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on service/controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository              userRepository;
    private final JwtAuthenticationEntryPoint jwtEntryPoint;

    // -------------------------------------------------------
    // UserDetailsService — loads user from DB by email
    // -------------------------------------------------------

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
    }

    // -------------------------------------------------------
    // PasswordEncoder — BCrypt (strength 12)
    // -------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // -------------------------------------------------------
    // AuthenticationProvider — wires UserDetailsService + encoder
    // -------------------------------------------------------

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Creates the JWT filter as an explicit bean to avoid circular wiring
     * between this configuration class and component scanning.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService
    ) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    // -------------------------------------------------------
    // AuthenticationManager — used in AuthService
    // -------------------------------------------------------

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // -------------------------------------------------------
    // Main Security Filter Chain
    // -------------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthFilter
    ) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Return 401 JSON instead of redirect on auth failure
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtEntryPoint)
            )

            // Stateless session — Spring Security won't create HttpSessions
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Route-level access rules
            .authorizeHttpRequests(auth -> auth
                // Public: registration, login, Swagger UI, health
                .requestMatchers(
                    "/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health"
                ).permitAll()

                // Admin-only: user management
                .requestMatchers("/users/**").hasRole("ADMIN")

                // Record creation/modification: ADMIN only
                .requestMatchers(HttpMethod.POST,   "/records/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/records/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/records/**").hasRole("ADMIN")

                // Record reading: ANALYST and ADMIN
                .requestMatchers(HttpMethod.GET,    "/records/**")
                    .hasAnyRole("ANALYST", "ADMIN")

                // Dashboard: all authenticated users (VIEWER can access)
                .requestMatchers("/dashboard/**")
                    .hasAnyRole("VIEWER", "ANALYST", "ADMIN")

                // Everything else: must be authenticated
                .anyRequest().authenticated()
            )

            // Plug in our JWT filter before Spring's username/password filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
