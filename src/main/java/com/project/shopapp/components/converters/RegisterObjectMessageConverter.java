package com.project.shopapp.components.converters;

import com.project.shopapp.dto.RegisterObject;
import com.project.shopapp.models.User;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
@Component
public class RegisterObjectMessageConverter extends JsonMessageConverter {
    public RegisterObjectMessageConverter() {
        super();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.addTrustedPackages("com.project.shopapp");
        typeMapper.setIdClassMapping(Collections.singletonMap("registerObject", RegisterObject.class));
        this.setTypeMapper(typeMapper);
    }
}
