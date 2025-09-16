package com.act.ecommerce.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "razorpay")
@Data
public class RazorpayConfig {
    private String key;
    private String secret;
    private String currency;

    // Getters and Setters
}
