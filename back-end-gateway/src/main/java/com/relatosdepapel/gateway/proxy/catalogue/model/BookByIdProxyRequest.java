package com.relatosdepapel.gateway.proxy.catalogue.model;

import lombok.Data;

import java.util.UUID;

@Data
public class BookByIdProxyRequest {

    private UUID externalId;
}