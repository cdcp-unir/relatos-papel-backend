package com.relatosdepapel.orders.controller.model;

import com.relatosdepapel.orders.service.CreateOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderService createOrderService;

    @PostMapping()
    public CreateOrderResponseDto CreateOrder(@RequestBody() CreateOrderRequestDto dto) {
        return createOrderService.CreateOrder(dto);
    }
}
