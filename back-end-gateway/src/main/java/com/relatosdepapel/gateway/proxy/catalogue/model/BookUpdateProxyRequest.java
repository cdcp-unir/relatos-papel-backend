package com.relatosdepapel.gateway.proxy.catalogue.model;

import lombok.Data;

import java.util.UUID;

@Data
public class BookUpdateProxyRequest<T> {

    private UUID externalId;
    private T data;
}