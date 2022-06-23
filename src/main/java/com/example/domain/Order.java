package com.example.domain;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * 注文(order)のdomain
 */

public class Order {

	/** DBのprimaryKeyの注文id */
	private Integer id;

	/** Userテーブルからもらってくる */
	private Integer userId;

	/** 現在の状態 0：注文前 1：未入金 2：入金済 */
	private Integer status;

	/** 合計金額 */
	private Integer totalPrice;

	/** 注文日 */
	private Date orderDate;

	/** 宛先氏名 */
	private String destinationName;

	/** 宛先メールアドレス */
	private String destinationEmail;

	/** 宛先郵便番号 */
	private String destinationZipcode;

	/** 宛先住所 */
	private String destinationAddress;

	/** 宛先電話番号 */
	private String destinationTel;

	/** 配達時間 */
	private Timestamp deliveryTime;

	/** 支払方法 */
	private Integer paymentMethod;

	/** domain のUser */
	private User user;

	/** カートに入った商品のリスト */
	private List<OrderItem> orderItemList;

	/**
	 * カートに入った商品リストの消費税合計を取得するメソッド
	 * 
	 * @return 合計消費税
	 */
	public int getTax() {
		int total = 0;
		for (OrderItem orderItem : orderItemList) {
			total += orderItem.getSubTotal();
		}
		int tax = (int) (total * 0.1);
		return tax;
	}

	/**
	 * カートに入った商品の合計金額（税込）を計算するメソッド
	 * 
	 * @return 商品合計金額（税込）
	 **/

	public int getCalcTotalPrice() {
		int tax = getTax();
		int itemPrice = 0;
		for (OrderItem item : orderItemList) {
			itemPrice += item.getSubTotal();
		}
		return tax + itemPrice;
	}

	public Order(Integer id, Integer userId, Integer status, Integer totalPrice, Date orderDate, String destinationName,
			String destinationEmail, String destinationZipcode, String destinationAddress, String destinationTel,
			Timestamp deliveryTime, Integer paymentMethod, User user, List<OrderItem> orderItemList) {
		super();
		this.id = id;
		this.userId = userId;
		this.status = status;
		this.totalPrice = totalPrice;
		this.orderDate = orderDate;
		this.destinationName = destinationName;
		this.destinationEmail = destinationEmail;
		this.destinationZipcode = destinationZipcode;
		this.destinationAddress = destinationAddress;
		this.destinationTel = destinationTel;
		this.deliveryTime = deliveryTime;
		this.paymentMethod = paymentMethod;
		this.user = user;
		this.orderItemList = orderItemList;
	}

	public Order() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public Integer getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Integer totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public String getDestinationEmail() {
		return destinationEmail;
	}

	public void setDestinationEmail(String destinationEmail) {
		this.destinationEmail = destinationEmail;
	}

	public String getDestinationZipcode() {
		return destinationZipcode;
	}

	public void setDestinationZipcode(String destinationZipcode) {
		this.destinationZipcode = destinationZipcode;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getDestinationTel() {
		return destinationTel;
	}

	public void setDestinationTel(String destinationTel) {
		this.destinationTel = destinationTel;
	}

	public Timestamp getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(Timestamp deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	public Integer getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(Integer paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<OrderItem> getOrderItemList() {
		return orderItemList;
	}

	public void setOrderItemList(List<OrderItem> orderItemList) {
		this.orderItemList = orderItemList;
	};

	@Override
	public String toString() {
		return "Order [id=" + id + ", userid=" + userId + ", status=" + status + ", totalPrice=" + totalPrice
				+ ", OrderDate=" + orderDate + ", destinationName=" + destinationName + ", destinationEmail="
				+ destinationEmail + ", destinationZipcode=" + destinationZipcode + ", destinationAddress="
				+ destinationAddress + ", destinationTel=" + destinationTel + ", deleveryTime=" + deliveryTime
				+ ", paymentMethod=" + paymentMethod + ", user=" + user + ", orderItemList=" + orderItemList + "]";
	}

}
