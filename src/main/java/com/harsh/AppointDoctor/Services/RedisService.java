package com.harsh.AppointDoctor.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.AppointDoctor.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public <T> T get(String key, TypeReference<T> typeRef) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return new ObjectMapper().readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void set(String key, Object o, Long ttl){
        try{
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key,json,ttl, TimeUnit.SECONDS);
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

}
