package com.relatosdepapel.comms_service.dto.gemini;

import lombok.Data;

import java.util.List;

@Data
public class Content {
    private List<Part> parts;
}
