package com.example.domain;

public class OrderItem {

	// DBのprimaryKeyのカート商品id
	private Integer id;
	// カートに入ってきた商品のid Itemテーブルのidをもらってくる
	private Integer itemId;
	// 同時に作るorderテーブルからidをもらってくる
	private Integer orderId;
	// フォームから送られてくる数量情報
	private Integer quantity;
	// Domain のItemの情報リスト
	private Item item;
  
  public int getSubTotal() {
		int price = item.getPrice();
		int total = price * quantity;
		return total;
	}

	public OrderItem() {
	};

	public OrderItem(Integer id, Integer itemId, Integer orderId, Integer quantity, Item item) {
		super();
		this.id = id;
		this.itemId = itemId;
		this.orderId = orderId;
		this.quantity = quantity;
		this.item = item;
	}

	@Override
	public String toString() {
		return "OrderItem [id=" + id + ", itemId=" + itemId + ", orderId=" + orderId + ", quantity=" + quantity
				+ ", item=" + item + "]";
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

}
