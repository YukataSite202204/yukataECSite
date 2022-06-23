package com.example.domain;

import java.util.Date;

/**
 * 注文履歴用のdomain
 * 
 * @author uehara
 * 
 **/
public class OrderHistory {
	
	/** ID */
	private Integer id;
	/** 注文ID */
	private Integer orderId;
	/** ユーザーID */
	private Integer userId;
	/** 支払・配送状況 */
	private Integer status;
	/** 注文日 */
	private Date orderDate;
	/** 商品ID */
	private Integer itemId;
	/** 商品名 */
	private String itemName;
	/** 商品金額 */
	private Integer price;
	/** 購入点数 */
	private Integer quantity;
	/** 商品画像のパス */
	private String imagePath;

	/**
	 * 各履歴の合計金額（税込）を取得するメソッド
	 * 
	 * @return 各履歴の合計金額（税込）
	 * 
	 */
	public Integer getTotalPrice() {
		Integer totalPrice = (int) ((price * quantity) * 1.1);
		return totalPrice;
	}

	public OrderHistory(Integer id, Integer orderId, Integer userId, Integer status, Date orderDate, Integer itemId,
			String itemName, Integer price, Integer quantity, String imagePath) {
		super();
		this.id = id;
		this.orderId = orderId;
		this.userId = userId;
		this.status = status;
		this.orderDate = orderDate;
		this.itemId = itemId;
		this.itemName = itemName;
		this.price = price;
		this.quantity = quantity;
		this.imagePath = imagePath;
	}

	public OrderHistory() {
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	@Override
	public String toString() {
		return "OrderHistory [orderid=" + orderId + ", userId=" + userId + ", status=" + status + ", totalPrice="
				+ ", orderDate=" + orderDate + ", itemId=" + itemId + ", itemName=" + itemName + ", price="
				+ price + ", quantity=" + quantity + ", imagePath=" + imagePath + "]";
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


}
