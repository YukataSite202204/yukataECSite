package com.example.domain;

/**
 * 
 * @author makarasu
 *
 */
public class Item {
	/** 商品ID */
	private Integer id;
	/** 商品名 */
	private String name;
	/** 商品説明 */
	private String description;
	/** 金額 */
	private Integer price;
	/** 商品画像パス */
	private String imagePath;
	/** 削除（trueなら削除） */
	private boolean deleted;
	/** 商品サイズ（基本はフリーサイズ） */
	private String size;
	/** 商品名の読み仮名 */
	private String kana;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}


	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public String getKana() {
		return kana;
	}

	public void setKana(String kana) {
		this.kana = kana;
	}

	public Item(Integer id, String name, String description, Integer price, String imagePath, boolean deleted,
			String size, String kana) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.imagePath = imagePath;
		this.deleted = deleted;
		this.size = size;
		this.kana = kana;
	}

	public Item() {
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", description=" + description + ", price=" + price
				+ ", imagePath=" + imagePath + ", deleted=" + deleted + ", size=" + size + ", kana=" + kana + "]";
	}



}

