package com.example.form;

/**
 * 商品検索用フォーム
 * 
 * @author makarasu
 *
 */
public class ItemSearchForm {
	/** 検索フォーム入力内容 */
	private String name;
	/** 並べ替えプルダウンメニュー value */
	private Integer order;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return "ItemSearchForm [name=" + name + ", order=" + order + "]";
	}


}
