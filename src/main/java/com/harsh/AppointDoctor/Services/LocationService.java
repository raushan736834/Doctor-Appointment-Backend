package com.harsh.AppointDoctor.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader(
                    HttpHeaders.USER_AGENT,
                    "AppointDoctor/1.0"
            )
            .build();

    public String reverseGeocode(
            double latitude,
            double longitude
    ) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/reverse")
                        .queryParam("lat", latitude)
                        .queryParam("lon", longitude)
                        .queryParam("format", "json")
                        .queryParam("addressdetails", 1)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        try {

            JsonNode root = objectMapper.readTree(response);

            JsonNode address = root.path("address");

            return getCity(address);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to parse location response",
                    e
            );
        }
    }

    private String getCity(JsonNode address) {

        // Different countries use different fields.
        if (address.hasNonNull("city")) {
            return address.get("city").asText();
        }

        if (address.hasNonNull("town")) {
            return address.get("town").asText();
        }

        if (address.hasNonNull("village")) {
            return address.get("village").asText();
        }

        if (address.hasNonNull("municipality")) {
            return address.get("municipality").asText();
        }

        return null;
    }
}
