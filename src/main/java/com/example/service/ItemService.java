
package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.Item;
import com.example.form.ItemSearchForm;
import com.example.repository.ItemRepository;

/**
 * 
 * @author makarasu
 *
 */
@Service
@Transactional
public class ItemService {

	@Autowired
	private ItemRepository itemRepository;

	/**
	 * 商品情報を全件検索<br>
	 * ページングのため、OFFSETを追加
	 * 
	 * @author mikami
	 */
	public List<Item> findAll(Integer nowpage) {
		return itemRepository.findAll(nowpage);
	}

	/** 全件検索した時の商品数を取得 */
	public int countOfFindAll() {
		return itemRepository.countOfFindAll();
	}

	/** idから商品検索 */
	public Item findById(Integer id) {
		return itemRepository.findById(id);
	}

	/** idから6つの商品をランダムに検索 */
	public List<Item> findByRandomId() {
		return itemRepository.findByRandomId();
	}

	/** 名前からあいまい検索 */
	public List<Item> findByName(ItemSearchForm form, Integer nowpage) {
		return itemRepository.findByName(form, nowpage);
	}

	/** あいまい検索した時の商品数を取得 */
	public int countOfFindByName(ItemSearchForm form) {
		return itemRepository.countOfFindByName(form);
	}

	/** オートコンプリート機能用商品名取得 */
	public List<String> findItemName() {
		return itemRepository.findItemName();
	}
	/**オートコンプリート機能商品名（読み仮名）取得*/
	public List<String> findItemNameKana(){
		return itemRepository.findItemNameKana();
	}

  /** 並べ替え結果取得 */
	public List<Item> changeOrder(ItemSearchForm form, Integer nowpage) {
		return itemRepository.changeOrder(form, nowpage);
	}

}



