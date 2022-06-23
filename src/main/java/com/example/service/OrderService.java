
package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.Order;
import com.example.domain.OrderHistory;
import com.example.repository.OrderRepository;

/**
 * 注文周りのserviceクラス
 * 
 */

@Service
@Transactional
public class OrderService {

	@Autowired
	private OrderRepository repository;
	
	public Order findOrder(Integer userId) {
		return repository.findByUserIdAndStatus(userId);
	}
	
	public void insert(Integer userId, Integer totalPrice) {
		repository.insert(userId, totalPrice);
	}

	public void upDate(Integer totalPrice, Integer orderId) {
		repository.upDate(totalPrice, orderId);
	}

	
	/** ユーザーIDでorder検索 */
	public Order findOrderById(Integer userId) {
		Order order = repository.findOrder(userId);
		return order;
	}
	
	/** 注文情報更新 */
	public void updateOrder(Order order) {
		repository.updateOrder(order);
	}
	
	/** 注文を表示する */
	public Order showOrder(Integer userId) {
		return repository.findByUserIdAndStatus(userId);
	}

	/** 注文履歴を取得する */
	public List<OrderHistory> getOrderHistory(Integer userId) {
		return repository.getOrderHistory(userId);
	}
	
	/** 注文履歴を更新する */
	public void addOrderHistory(OrderHistory orderHistory) {
		repository.addOrderHistory(orderHistory);
	}
}
