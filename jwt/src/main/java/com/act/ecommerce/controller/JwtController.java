package com.act.ecommerce.controller;

import com.act.ecommerce.entity.JwtRequest;
import com.act.ecommerce.entity.JwtResponse;
import com.act.ecommerce.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class JwtController {

    @Autowired
    JwtService jwtService;

    @PostMapping({"/authenticate"})
    public JwtResponse createJWTToken(@RequestBody JwtRequest jwtRequest) throws Exception {
        // This method is used to create a JWT token
        // The actual implementation of token creation is not shown here
        // You would typically call a service method to generate the token

        return jwtService.createToken(jwtRequest);
    }

}
