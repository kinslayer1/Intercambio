package avvillas.template_admin.web.configuration.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filter for API key authentication.
 * This filter checks for an API key in the request header and authenticates the request if the key is valid.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String apiKeyHeaderName;
    private final Map<String, List<String>> apiKeys;

    /**
     * Constructor for ApiKeyAuthFilter.
     *
     * @param apiKeyHeaderName The name of the header that contains the API key
     * @param apiKeys A map of API keys to role IDs
     */
    public ApiKeyAuthFilter(String apiKeyHeaderName, Map<String, List<String>> apiKeys) {
        this.apiKeyHeaderName = apiKeyHeaderName;
        this.apiKeys = apiKeys;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip filter if authentication is already set
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(apiKeyHeaderName);

        if (apiKey != null && apiKeys.containsKey(apiKey)) {
            List<String> roles = apiKeys.get(apiKey);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("API_USER", null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}