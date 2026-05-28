package com.relatosdepapel.gateway.proxy.orders.model;

import lombok.Data;

@Data
public class GetOrdersByOwnerProxyRequest {
    private Integer ownerId;
}