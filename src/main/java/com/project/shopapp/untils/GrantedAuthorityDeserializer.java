package com.project.shopapp.untils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GrantedAuthorityDeserializer extends JsonDeserializer<List<SimpleGrantedAuthority>> {

    @Override
    public List<SimpleGrantedAuthority> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        JsonNode node = p.getCodec().readTree(p);
        for (JsonNode authorityNode : node) {
            authorities.add(new SimpleGrantedAuthority(authorityNode.asText()));
        }
        return authorities;
    }
}
