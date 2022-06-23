
package com.example.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.example.domain.Order;
import com.example.domain.OrderHistory;

@Repository
public class OrderRepository {

	@Autowired
	private NamedParameterJdbcTemplate template;

	/** 注文確認時のRowMapper */
	private static final RowMapper<Order> ORDER_ROW_MAPPER = (rs, i) -> {
		Order order = new Order();
		order.setId(rs.getInt("id"));
		order.setUserId(rs.getInt("user_id"));
		order.setStatus(rs.getInt("status"));
		order.setTotalPrice(rs.getInt("total_price"));
		return order;
	};

	/** 注文時のRowMapper */
	private static final RowMapper<Order> ORDER_CONFIRM_ROW_MAPPER = (rs, i) -> {
		Order order = new Order();
		order.setId(rs.getInt("id"));
		order.setUserId(rs.getInt("user_id"));
		order.setStatus(rs.getInt("status"));
		order.setTotalPrice(rs.getInt("total_price"));
		order.setOrderDate(rs.getDate("order_date"));
		order.setDestinationName(rs.getString("destination_name"));
		order.setDestinationEmail(rs.getString("destination_email"));
		order.setDestinationZipcode(rs.getString("destination_zipcode"));
		order.setDestinationAddress(rs.getString("destination_address"));
		order.setDestinationTel(rs.getString("destination_tel"));
		order.setDeliveryTime(rs.getTimestamp("delivery_time"));
		order.setPaymentMethod(rs.getInt("payment_method"));
		return order;
	};

	/** 注文履歴取得用のRowMapper */
	private static final RowMapper<OrderHistory> ORDERHISTORY_ROW_MAPPER = (rs, i) -> {
		OrderHistory orderHistory = new OrderHistory();
		orderHistory.setId(rs.getInt("id"));
		orderHistory.setStatus(rs.getInt("status"));
		orderHistory.setOrderId(rs.getInt("order_id"));
		orderHistory.setUserId(rs.getInt("user_id"));
		orderHistory.setOrderDate(rs.getDate("order_date"));
		orderHistory.setItemId(rs.getInt("item_id"));
		orderHistory.setItemName(rs.getString("item_name"));
		orderHistory.setPrice(rs.getInt("item_price"));
		orderHistory.setQuantity(rs.getInt("quantity"));
		orderHistory.setImagePath(rs.getString("image_path"));
		return orderHistory;

	};

	/** userIDとstatusからORDERを検索 */
	public Order findByUserIdAndStatus(Integer userId) {
		String sql = "SELECT id, user_id, status, total_price FROM orders WHERE user_id=:userId AND status=0";
		SqlParameterSource param = new MapSqlParameterSource().addValue("userId", userId);
		try {
			Order order = template.queryForObject(sql, param, ORDER_ROW_MAPPER);
			return order;
		} catch (EmptyResultDataAccessException e) {
			Order order = null;
			return order;
		}
	}

	/** userIDと合計金額を渡してインサート */
	public void insert(Integer userId, Integer totalPrice) {

		String sql = "INSERT INTO orders(user_id, status, total_price) VALUES(:userId, 0, :totalPrice)";
		SqlParameterSource param = new MapSqlParameterSource().addValue("userId", userId).addValue("totalPrice",
				totalPrice);
		template.update(sql, param);
	}

	/** カートに追加で商品を入れる場合、合計金額のみupDataされる */
	public void upDate(Integer totalPrice, Integer orderId) {

		String sql = "UPDATE orders SET total_price=:totalPrice WHERE id=:id";
		SqlParameterSource param = new MapSqlParameterSource().addValue("totalPrice", totalPrice).addValue("id",
				orderId);
		template.update(sql, param);
	}

	/**
	 * ユーザーIDで該当の注文情報をDBから取得するメソッド
	 * 
	 * @param userId ユーザーID
	 * @return ある場合は注文情報
	 */
	public Order findOrder(Integer userId) {
		String sql = "SELECT id, user_id, status, total_price, order_date, destination_name, destination_email"
				+ ", destination_zipcode, destination_address, destination_tel, delivery_time"
				+ ", payment_method FROM orders WHERE user_id=:id AND status=0"; //
		SqlParameterSource param = new MapSqlParameterSource().addValue("id", userId);
		try {
			Order order = template.queryForObject(sql, param, ORDER_CONFIRM_ROW_MAPPER);
			return order;
		} catch (RuntimeException e) {
			return null;
		}

	}

	/**
	 * 注文情報をordersテーブルに反映させる（updateする）メソッド
	 * 
	 * @param order 注文情報
	 * 
	 */
	public void updateOrder(Order order) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(order);
		String sql = "UPDATE orders SET status=:status, order_date=:orderDate, destination_name=:destinationName, destination_email=:destinationEmail"
				+ ", destination_zipcode=:destinationZipcode, destination_address=:destinationAddress"
				+ ", destination_tel=:destinationTel, delivery_time=:deliveryTime, payment_method=:paymentMethod WHERE id=:id";

		template.update(sql, param);
	}

	/**
	 * 注文確定時、orderHistoryテーブルを更新するメソッド
	 * 
	 * @param orderHistory 注文履歴
	 */
	public void addOrderHistory(OrderHistory orderHistory) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(orderHistory);
		String sql = "INSERT INTO order_histories (order_id, user_id, status, order_date, item_id"
				+ ", item_name, item_price, image_path, quantity)"
				+ " values(:orderId, :userId, :status, :orderDate, :itemId, :itemName, :price, :imagePath, :quantity)";
		template.update(sql, param);
	}

	/**
	 * 注文履歴をorderHistoryテーブルから持ってくるメソッド
	 * 
	 * @param userId ユーザーID
	 * @return orderHistoryList 該当ユーザーの注文履歴リスト
	 */
	public List<OrderHistory> getOrderHistory(Integer userId) {
		String sql = "SELECT id, order_id, user_id, status, order_date, item_id, item_name, item_price"
				+ ", image_path, quantity FROM order_histories WHERE user_id=:id ORDER BY order_date DESC";
		SqlParameterSource param = new MapSqlParameterSource().addValue("id", userId);
		List<OrderHistory> orderHistoryList = template.query(sql, param, ORDERHISTORY_ROW_MAPPER);
		return orderHistoryList;
	}

}
