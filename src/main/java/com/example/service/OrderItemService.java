package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.OrderItem;
import com.example.repository.OrderItemRepository;

@Service
@Transactional
public class OrderItemService {

	@Autowired
	private OrderItemRepository repository;

	public OrderItem insert(OrderItem orderItem) {
		return repository.insert(orderItem);
	}

	public void delete(Integer id) {
		repository.delete(id);
	}
}
