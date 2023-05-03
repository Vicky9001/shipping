package com.example.shipping.repository;

import com.example.shipping.models.Order;
import com.example.shipping.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCarrier(User carrier);
}
