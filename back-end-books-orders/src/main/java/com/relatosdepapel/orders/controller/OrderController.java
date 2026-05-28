package com.relatosdepapel.orders.controller;

import com.relatosdepapel.orders.controller.model.CreateOrderRequestDto;
import com.relatosdepapel.orders.controller.model.CreateOrderResponseDto;
import com.relatosdepapel.orders.controller.model.GetOrdersResponseDto;
import com.relatosdepapel.orders.service.CreateOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{ownerId}")
    public ResponseEntity<GetOrdersResponseDto> GetOrderByOwnerId(@PathVariable Integer ownerId) {
        return ResponseEntity.ok(createOrderService.getOrderByOwnerId(ownerId));
    }
}
