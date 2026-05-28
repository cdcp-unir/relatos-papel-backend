package com.relatosdepapel.orders.repository;

import com.relatosdepapel.orders.repository.model.Order;
import com.relatosdepapel.orders.repository.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Integer> {
    List<Order> findByOwnerIdOrderByOrderDateDesc(Integer ownerId);

    List<Order> findByStatus(OrderStatus status);
}
