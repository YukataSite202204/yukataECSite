package com.example.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.example.domain.Item;
import com.example.form.ItemSearchForm;

/**
 * 
 * @author makarasu
 *
 */
@Repository
public class ItemRepository {
	private static final RowMapper<Item> ITEM_ROW_MAPPER = (rs, i) -> {
		Item item = new Item();
		item.setId(rs.getInt("id"));
		item.setName(rs.getString("name"));
		item.setDescription(rs.getString("description"));
		item.setPrice(rs.getInt("price"));
		item.setImagePath(rs.getString("image_path"));
		item.setDeleted(rs.getBoolean("deleted"));
		item.setSize(rs.getString("size"));
		item.setKana(rs.getString("kana"));
		return item;
	};
	
	/**
	 * SQLで集約関数を返すためのローマッパー. <br>
	 * Pagingを行う時に使います
	 * 
	 * @author mikami
	 */
	private static final RowMapper<Integer> AGGREGATE_FUNCTION_ROW_MAPPER = (rs, i) -> {
		Integer result = rs.getInt("count");
		return result;
	};

	/**
	 * オートコンプリート機能用
	 * 
	 * @author makarasu
	 */
	private static final RowMapper<String> AUTO_COMPLETE_ROW_MAPPER = (rs, i) -> {
		String name = rs.getString("name");
		return name;
	};
	/**
	 * オートコンプリート機能用（読み仮名）
	 * 
	 * @author makarasu
	 */
	private static final RowMapper<String> AUTO_COMPLETE_KANA_ROW_MAPPER = (rs, i) -> {
		String kana = rs.getString("kana");
		return kana;
	};

	@Autowired
	private NamedParameterJdbcTemplate template;

	/**
	 * 商品情報を全件検索<br>
	 * ページングのため、OFFSETを追加
	 * 
	 * @return 検索結果を返す
	 * @author mikami
	 */
	public List<Item> findAll(Integer nowpage) {
		String sql = "SELECT id ,name ,description ,price ,image_path ,deleted ,size ,kana FROM items ORDER BY price, kana "
				+ "OFFSET 12 * (:nowpage - 1) LIMIT 12;";
		SqlParameterSource param = new MapSqlParameterSource().addValue("nowpage", nowpage);
		List<Item> itemList = template.query(sql, param, ITEM_ROW_MAPPER);
		return itemList;
	}

	/**
	 * 全件検索した時の商品数を取得する.
	 * 
	 * @return 全件検索した時の商品数を返す
	 * @author mikami
	 */
	public int countOfFindAll() {
		String sql = "SELECT count(*) FROM items";
		List<Integer> count = template.query(sql, AGGREGATE_FUNCTION_ROW_MAPPER);
		return count.get(0);
	}

	/**
	 * 取得したid（クリックされた商品）から商品情報を検索する
	 * 
	 * @param id 指定されたid
	 * @return 検索結果を返す
	 * @author makarasu
	 */

	public Item findById(Integer id) {
		String sql = "SELECT id ,name ,description ,price ,image_path ,deleted ,size ,kana FROM items WHERE id=:id ;";
		SqlParameterSource param = new MapSqlParameterSource().addValue("id", id);
		Item item = template.queryForObject(sql, param, ITEM_ROW_MAPPER);
		return item;
	}


	/**
	 * 商品情報を無作為に6件検索する ※未使用
	 * 
	 * @param ランダムで出した6個の数字（範囲：1~18)
	 * @return 検索結果を返す
	 * @author mikami
	 * 
	 */
	public List<Item> findByRandomId() {
		ArrayList<Integer> randomList = new ArrayList<Integer>();

		// 1～18の整数値を持つリストを用意
		for (int i = 1; i <= 18; i++) {
			randomList.add(i);
		}

		// shuffleメソッドで上で作ったリストをシャッフル
		Collections.shuffle(randomList);
		
		String sql = "SELECT id ,name ,description ,price ,image_path ,deleted ,size ,kana "
				+ "FROM items "
				+ "WHERE id=:id0 " + "OR id=:id1 " + "OR id=:id2 " + "OR id=:id3 " + "OR id=:id4 " + "OR id=:id5 ;";
		SqlParameterSource param = new MapSqlParameterSource().addValue("id0", randomList.get(0))
				.addValue("id1", randomList.get(1)).addValue("id2", randomList.get(2))
				.addValue("id3", randomList.get(3)).addValue("id4", randomList.get(4))
				.addValue("id5", randomList.get(5));
		List<Item> itemList = template.query(sql, param, ITEM_ROW_MAPPER);
		return itemList;
	}
	
	/**
	 * 商品情報をあいまい検索する ページングのため、OFFSETを追加
	 * 
	 * @param form フォーム
	 * @return 検索結果を返す
	 * @author makarasu (ページング機能追加:mikami)
	 */
	public List<Item> findByName(ItemSearchForm form, Integer nowpage) {
		String sql = "SELECT id, name, description, price, image_path, deleted ,size ,kana "
				+ "FROM items "
				+ "WHERE name LIKE :name OR kana LIKE :kana "
				+ "ORDER BY price, kana "
				+ "OFFSET 12 * (:nowpage - 1) LIMIT 12;";
		SqlParameterSource param = new MapSqlParameterSource().addValue("name", "%" + form.getName() + "%")
				.addValue("kana", "%" + form.getName() + "%")
				.addValue("nowpage", nowpage);
		List<Item> itemList = template.query(sql, param, ITEM_ROW_MAPPER);
		return itemList;
	}

	/**
	 * 商品情報をあいまい検索した時の商品数を取得する.
	 * 
	 * @return あいまい検索した時の商品数を返す
	 * @author mikami
	 */
	public int countOfFindByName(ItemSearchForm form) {
		String sql = "SELECT count(*) FROM items WHERE name LIKE :name OR kana LIKE :kana";
		SqlParameterSource param = new MapSqlParameterSource().addValue("name", "%" + form.getName() + "%")
				.addValue("kana", "%" + form.getName() + "%");
		List<Integer> count = template.query(sql, param, AGGREGATE_FUNCTION_ROW_MAPPER);
		if (count.get(0) == 0) {
			String sql2 = "SELECT count(*) FROM items";
			List<Integer> count2 = template.query(sql2, AGGREGATE_FUNCTION_ROW_MAPPER);
			return count2.get(0);
		} else {
		return count.get(0);
		}
	}

	/**
	 * 商品名を取得する（オートコンプリート機能用）
	 * 
	 * @return 取得結果を返す
	 * @author makarasu
	 */
	public List<String> findItemName() {
		String sql = "SELECT name FROM items ORDER BY id;";
		List<String> nameList = template.query(sql, AUTO_COMPLETE_ROW_MAPPER);
		return nameList;
	}
	
	/**
	 * 商品名（読み仮名）を取得する（オートコンプリート機能用）
	 * 
	 * @return 取得結果を返す
	 * @author makarasau
	 */
	public List<String> findItemNameKana() {
		String sql = "SELECT kana FROM items ORDER BY id;";
		List<String> kanaList = template.query(sql, AUTO_COMPLETE_KANA_ROW_MAPPER);
		return kanaList;
	}

	/**
	 * 選択されたプルダウンに応じて並び替えをする 検索フォームの名前が全件検索結果を返す
	 * 
	 * @param form  フォーム
	 * @param order 選択されたプルダウンから取得する値
	 * @return 取得結果を返す
	 * @author makarasu
	 */
	public List<Item> changeOrder(ItemSearchForm form, Integer nowPage) {
		if (form.getOrder() == 0) { // 金額が安い順（金額が同じならその中で商品名昇順）
			List<Item> itemList = findByName(form, nowPage);
			if (itemList.size() == 0) {
				itemList = findAll(nowPage);
			}
			return itemList;

		} else if (form.getOrder() == 1) { // 金額が高い順（金額が同じならその中で商品名降順）
			String sql = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
					+ "WHERE name LIKE :name OR kana LIKE :kana ORDER BY price DESC, kana OFFSET 12 * (:nowpage - 1) LIMIT 12;";
			SqlParameterSource param = new MapSqlParameterSource().addValue("name", "%" + form.getName() + "%")
					.addValue("kana", "%" + form.getName() + "%")
					.addValue("nowpage", nowPage);
			List<Item> itemList = template.query(sql, param, ITEM_ROW_MAPPER);
			// あいまい検索結果が「該当商品なし」の場合、全件検索結果を取得する
			if (itemList.size() == 0) {
				String sql2 = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
						+ " ORDER BY price DESC, kana OFFSET 12 * (:nowpage - 1) LIMIT 12;";
				SqlParameterSource param2 = new MapSqlParameterSource().addValue("nowpage", nowPage);
				itemList = template.query(sql2, param2, ITEM_ROW_MAPPER);
			}
			return itemList;

		} else if (form.getOrder() == 2) { // 商品名昇順
			String sql = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
					+ "WHERE name LIKE :name OR kana LIKE :kana ORDER BY kana OFFSET 12 * (:nowpage - 1) LIMIT 12;";
			SqlParameterSource param = new MapSqlParameterSource().addValue("name", "%" + form.getName() + "%")
					.addValue("kana", "%" + form.getName() + "%")
					.addValue("nowpage", nowPage);
			List<Item> itemList = template.query(sql, param, ITEM_ROW_MAPPER);
			// あいまい検索結果が「該当商品なし」の場合、全件検索結果を取得する
			if (itemList.size() == 0) {
				String sql2 = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
						+ " ORDER BY kana OFFSET 12 * (:nowpage - 1) LIMIT 12;";
				SqlParameterSource param2 = new MapSqlParameterSource().addValue("nowpage", nowPage);
				itemList = template.query(sql2, param2, ITEM_ROW_MAPPER);
			}
			return itemList;

		} else if (form.getOrder() == 3) { // 商品名降順
			String sql = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
					+ "WHERE name LIKE :name OR kana LIKE :kana ORDER BY kana DESC OFFSET 12 * (:nowpage - 1) LIMIT 12;";
			SqlParameterSource param = new MapSqlParameterSource().addValue("name", "%" + form.getName() + "%")
					.addValue("kana", "%" + form.getName() + "%")
					.addValue("nowpage", nowPage);
			List<Item> itemList = template.query(sql, param, ITEM_ROW_MAPPER);
			// あいまい検索結果が「該当商品なし」の場合、全件検索結果を取得する
			if (itemList.size() == 0) {
				String sql2 = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
						+ " ORDER BY kana DESC OFFSET 12 * (:nowpage - 1) LIMIT 12;";
				SqlParameterSource param2 = new MapSqlParameterSource().addValue("nowpage", nowPage);
				itemList = template.query(sql2, param2, ITEM_ROW_MAPPER);
			}
			return itemList;

		} else if (form.getOrder() == 4) { // 発売日が古い順（id昇順）
			String sql = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
					+ "WHERE name LIKE :name OR kana LIKE :kana ORDER BY id OFFSET 12 * (:nowpage - 1) LIMIT 12;";
			SqlParameterSource param = new MapSqlParameterSource().addValue("name", "%" + form.getName() + "%")
					.addValue("kana", "%" + form.getName() + "%")
					.addValue("nowpage", nowPage);
			List<Item> itemList = template.query(sql, param, ITEM_ROW_MAPPER);
			// あいまい検索結果が「該当商品なし」の場合、全件検索結果を取得する
			if (itemList.size() == 0) {
				String sql2 = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
						+ " ORDER BY id OFFSET 12 * (:nowpage - 1) LIMIT 12;";
				SqlParameterSource param2 = new MapSqlParameterSource().addValue("nowpage", nowPage);
				itemList = template.query(sql2, param2, ITEM_ROW_MAPPER);
			}
			return itemList;

		} else { // 発売日が新しい順（id降順）
			String sql = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
					+ "WHERE name LIKE :name OR kana LIKE :kana ORDER BY id DESC OFFSET 12 * (:nowpage - 1) LIMIT 12;";
			SqlParameterSource param = new MapSqlParameterSource().addValue("name", "%" + form.getName() + "%")
					.addValue("kana", "%" + form.getName() + "%")
					.addValue("nowpage", nowPage);
			List<Item> itemList = template.query(sql, param, ITEM_ROW_MAPPER);
			// あいまい検索結果が「該当商品なし」の場合、全件検索結果を取得する
			if (itemList.size() == 0) {
				String sql2 = "SELECT id, name, description, price, image_path, deleted ,size ,kana FROM items "
						+ " ORDER BY id DESC OFFSET 12 * (:nowpage - 1) LIMIT 12;";
				SqlParameterSource param2 = new MapSqlParameterSource().addValue("nowpage", nowPage);
				itemList = template.query(sql2, param2, ITEM_ROW_MAPPER);
			}
			return itemList;
		}
	}

}

