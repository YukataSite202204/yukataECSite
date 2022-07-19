package com.example.controller;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.domain.Item;
import com.example.form.ItemSearchForm;
import com.example.service.ItemService;

/**
 * 
 * @author makarasu
 *
 */
@Controller
@RequestMapping("/yukata")
public class ItemController {

	@Autowired
	private ItemService itemService;

	@Autowired
	private HttpSession session;

	@ModelAttribute
	public ItemSearchForm setUpForm() {
		return new ItemSearchForm();
	}

	/**
	 * 商品一覧ページを表示
	 * 
	 * @param model リクエストスコープ
	 * @return 商品一覧ページにフォワード
	 */
	@RequestMapping("/list")
	public Object index(ItemSearchForm form, String nowPage, String boxFlg, String boxNumber,
			String nextBtnFlg, String beforeBtnFlg,
			Model model) {
		session.setAttribute("calledSource", "index");
		session.removeAttribute("name");
		session.removeAttribute("message");
		// オートコンプリート機能用
		List<String> nameList = itemService.findItemName();
		session.setAttribute("nameList", nameList);
		List<String> kanaList = itemService.findItemNameKana();
		session.setAttribute("kanaList", kanaList);
		return paging(form, nowPage, boxFlg, boxNumber, nextBtnFlg, beforeBtnFlg, "index", model);
	}

	/**
	 * トップページを表示
	 * 
	 * @return トップページにフォワード
	 */
	@RequestMapping("/top")
	public String top() {
		return "top";
	}

	/**
	 * 商品詳細ページを表示
	 * 
	 * @param id    表示する商品のid
	 * @param model リクエストスコープ
	 * @return 商品詳細ページにフォワード
	 */
	@RequestMapping("/detail")
	public String detail(Integer id, Model model) {
		Item item = itemService.findById(id);
		model.addAttribute("item", item);
		return "item_detail";
	}

	/**
	 * あいまい検索が実行された条件で、ページング処理に遷移する
	 * 
	 * @param form  検索用フォーム
	 * @param model リクエストスコープ
	 * @return 検索結果を格納し、商品一覧ページにフォワード
	 */
	@RequestMapping("/search")
	public String search(ItemSearchForm form, Integer order, String nowPage, String boxFlg, String boxNumber,
			String nextBtnFlg, String beforeBtnFlg, Model model) {
		session.setAttribute("calledSource", "search");
		session.setAttribute("name", form.getName());
			return paging(form, nowPage, boxFlg, boxNumber, nextBtnFlg, beforeBtnFlg, "search", model);
	}

	/**
	 * 並び替えが実行された条件でページング処理に遷移する
	 * 
	 * @param form         検索用フォーム
	 * @param nowPage      現在のページ
	 * @param boxFlg
	 * @param boxNumber
	 * @param nextBtnFlg
	 * @param beforeBtnFlg
	 * @param model
	 * @return ページングメソッドを返す
	 */
	@RequestMapping("/order")
	public Object changeOrder(ItemSearchForm form, String nowPage, String boxFlg, String boxNumber, String nextBtnFlg,
			String beforeBtnFlg, Model model) {
		session.setAttribute("calledSource", "order");
		session.setAttribute("name", form.getName());
		session.setAttribute("order", form.getOrder());
		return paging(form, nowPage, boxFlg, boxNumber, nextBtnFlg, beforeBtnFlg, "order", model);
	}

	/**
	 * ページング共通処理 <br>
	 * 
	 * @author mikami
	 * @param nowPage：現在のページ
	 * @param boxFlg：ボックスを作るかどうか判定するフラグ(ボックスを作る場合："1",ボックスを作らない場合:null）
	 * @param boxNumber：押下されたボックス番号
	 * @param nextBtnFlg：「次へ」が押下されたかどうか判定するフラグ（押下された場合："1",押下されていない場合:null）
	 * @param beforeBtnFlg：「前へ」が押下されたかどうか判定するフラグ（押下された場合："1",押下されていない場合:null）
	 * @return 商品一覧ページに遷移
	 */
	@RequestMapping("/page")
	public String paging(
			ItemSearchForm form,
			String nowPage, 
			String boxFlg, 
			String boxNumber, 
			String nextBtnFlg, 
			String beforeBtnFlg,
			String calledSource, 
			Model model
			) {
				// String name = (String) session.getAttribute("name");

		// 現在のページがどこか調べる。nullだったらページングの始まりであるため、"1"をセットする。
		if (nowPage == null) {
			nowPage = "1";
		}

		// nowPageをInteger型に変換
		Integer intNowpage = Integer.parseInt(nowPage);

		// 初回の場合、ボックスサイズを作成する。
		if (boxFlg == null) {
			Integer boxSize; // ボックスのサイズ
			Integer boxSizeMax = null; // ボックスの最大サイズ
			Integer surplus; // ボックスサイズを作成する計算のために必要な変数

			if (calledSource.equals("index")) {
				boxSize = itemService.countOfFindAll();
				boxSizeMax = boxSize / 12;
				surplus = boxSize % 12;
				if (surplus != 0) {
					boxSizeMax++;
				}
			} else if (calledSource.equals("search")) {
				boxSize = itemService.countOfFindByName(form);
				boxSizeMax = boxSize / 12;
				surplus = boxSize % 12;
				if (surplus != 0) {
					boxSizeMax++;
				}
			} else if (calledSource.equals("order")) {
				boxSize = itemService.countOfFindByName(form);
				boxSizeMax = boxSize / 12;
				surplus = boxSize % 12;
				if (surplus != 0) {
					boxSizeMax++;
				}
			}
			List<Integer> boxSizeList = new LinkedList<>();
			for (int i = 1; i <= boxSizeMax; i++) {
				boxSizeList.add(i);
			}
			// タイムリーフで表示させるために、セッションにボックスサイズを格納
			session.setAttribute("boxSizeMax", boxSizeMax);
			session.setAttribute("boxSizeList", boxSizeList);
		}


		// 「次へ」が押されたときの処理
		if (!(nextBtnFlg == null)) {
			intNowpage++;
			if (calledSource.equals("index")) {
				List<Item> itemList = itemService.findAll(intNowpage);
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			} else if (calledSource.equals("search")) {
				if (form.getName() == null) {
					form.setName((String) session.getAttribute("name"));
				}
				List<Item> itemList = itemService.findByName(form, intNowpage);
				if (itemList.size() == 0) {
					itemList = itemService.findAll(intNowpage);
				}
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			} else if (calledSource.equals("order")) {
				if (form.getOrder() == null) {
					form.setName((String) session.getAttribute("name"));
					form.setOrder((Integer) session.getAttribute("order"));
				}
				List<Item> itemList = itemService.changeOrder(form, intNowpage);
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			}
		}

		// 「前へ」が押されたときの処理
		if (!(beforeBtnFlg == null)) {
			intNowpage--;
			if (calledSource.equals("index")) {
				List<Item> itemList = itemService.findAll(intNowpage);
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			} else if (calledSource.equals("search")) {
				if (form.getName() == null) {
					form.setName((String) session.getAttribute("name"));
				}
				List<Item> itemList = itemService.findByName(form, intNowpage);
				if (itemList.size() == 0) {
					itemList = itemService.findAll(intNowpage);
				}
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			} else if (calledSource.equals("order")) {
				if (form.getOrder() == null) {
					form.setName((String) session.getAttribute("name"));
					form.setOrder((Integer) session.getAttribute("order"));
				}
				List<Item> itemList = itemService.changeOrder(form, intNowpage);
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			}
		}

		// ボックスボタンが押されたときの処理
		if (!(boxNumber == null)) {
			Integer intBoxnumber = Integer.parseInt(boxNumber);
			intNowpage = intBoxnumber;
			if (calledSource.equals("index")) {
				List<Item> itemList = itemService.findAll(intNowpage);
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			} else if (calledSource.equals("search")) {
				if (form.getName() == null) {
					form.setName((String) session.getAttribute("name"));
				}
				List<Item> itemList = itemService.findByName(form, intNowpage);
				if (itemList.size() == 0) {
					itemList = itemService.findAll(intNowpage);
				}
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			} else if (calledSource.equals("order")) {
				if (form.getOrder().equals(null)) {
					form.setName((String) session.getAttribute("name"));
					form.setOrder((Integer) session.getAttribute("order"));
				}
				List<Item> itemList = itemService.changeOrder(form, intNowpage);
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			}
		}

		// 「次へ」「前へ」ボックスボタン いずれも押下がないとき。すなわち初回表示の時
		if (boxFlg == null) {
			if (calledSource.equals("index")) {
				List<Item> itemList = itemService.findAll(intNowpage);
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				boxFlg = "1";
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			} else if (calledSource.equals("search")) {
				if (form.getName() == null) {
					form.setName((String) session.getAttribute("name"));
				}
				List<Item> itemList = itemService.findByName(form, intNowpage);
				if (itemList.size() == 0) {
					if (form.getName() == null) { // 検索結果クリアボタンを押すとnullが返ってくるため、その場合はセッションのメッセージを削除する
						session.removeAttribute("message");
					} else {
						String message = "条件にマッチする商品はありません。";
						session.setAttribute("message", message);
					}
					itemList = itemService.findAll(Integer.valueOf(nowPage));
				} else {
					String message = "検索結果：" + itemService.countOfFindByName(form) + "件";
					model.addAttribute("itemCount", itemService.countOfFindByName(form));
					session.setAttribute("message", message);
				}
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				boxFlg = "1";
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			} else if (calledSource.equals("order")) {
				if (form.getOrder() == null) {
					form.setName((String) session.getAttribute("name"));
					form.setOrder((Integer) session.getAttribute("order"));
				}
				List<Item> itemList = itemService.changeOrder(form, intNowpage);
				model.addAttribute("itemList", itemList);
				model.addAttribute("nowPage", intNowpage);
				boxFlg = "1";
				model.addAttribute("boxFlg", boxFlg);
				return "item_list";
			}
		}
		return "item_list";
	}
}
