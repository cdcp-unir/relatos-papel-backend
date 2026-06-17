package com.relatosdepapel.comms_service.service;

import com.relatosdepapel.comms_service.dto.gemini.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiChatService {
    private final WebClient geminiWebClient;

    public String generateResponse(String message) {
        Map<String, Object> request = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", message)
                                }
                        ),
                }
        );

        try {
            GeminiResponse response = geminiWebClient.post()
                    .uri("/v1beta/models/gemini-2.5-flash:generateContent")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
                return "No se obtuvo respuesta de Gemini";
            }

            return response.getCandidates()
                    .getFirst()
                    .getContent()
                    .getParts()
                    .getFirst()
                    .getText();

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
