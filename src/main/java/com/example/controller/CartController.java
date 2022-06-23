package com.example.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.domain.Item;
import com.example.domain.Order;
import com.example.domain.OrderItem;
import com.example.domain.User;
import com.example.service.ItemService;
import com.example.service.OrderItemService;
import com.example.service.OrderService;

@Controller
@RequestMapping("/shoppingCart")
public class CartController {

	@Autowired
	private HttpSession session;

	@Autowired
	private ItemService itemService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderItemService orderItemService;
	
	
	/**
	 * カートに入れた商品・合計金額を表示するメソッド
	 * 
	 * @param quantity　商品の数量
	 * @param itemId　該当商品のid
	 * @param model
	 * @return sessionｽｺｰﾌﾟに注文商品のListを詰め、ショッピングカートを表示する画面へ遷移する。
	 */
	@SuppressWarnings({ "unchecked" })
	@RequestMapping("/inCart")
	public String inCart(Integer quantity, Integer itemId, Model model) {
		List<OrderItem> orderItemList = (List<OrderItem>) session.getAttribute("orderItemList");
		User user = (User) session.getAttribute("user");
		Order order = new Order();
		// ログインしていない状態でアクセスしようとした場合、ログイン画面を表示する
		if (user == null) {
			model.addAttribute("loginErrorFromCart", "ログインしてください");
			return "forward:/user/tologin";
		}
		
		// sessionのorderItemListが空であれば、新規作成しsessionスコープにつめる
		if (orderItemList == null) {
			orderItemList = new ArrayList<>();
			session.setAttribute("orderItemList", orderItemList);
		}

		// 引数のquantityとitemIdの値が入っていない場合（商品追加以外でショッピングカートのページにアクセスした場合）
		if (quantity == null && itemId == null) {

			// orderItemListに値が入っていなければ、「カートに商品がありません」と表示
			if (orderItemList.isEmpty()) {
				model.addAttribute("nullCartMessage", "カートに商品がありません");
			}

			// 消費税と合計金額の計算
			order.setOrderItemList(orderItemList);
			int totalPrice = order.getCalcTotalPrice();
			int tax = order.getTax();
			model.addAttribute("totalPrice", totalPrice);
			model.addAttribute("tax", tax);
			return "cart_list";

		} else {
			 Integer userId = user.getId();

			// neworderItemオブジェクトに値をセット
			OrderItem newOrderItem = new OrderItem();
			newOrderItem.setItemId(itemId);
			newOrderItem.setQuantity(quantity);
			Item itemObject = itemService.findById(itemId);
			newOrderItem.setItem(itemObject);

			// セッションスコープにorderItemListを格納する
			orderItemList.add(newOrderItem);
			session.setAttribute("orderItemList", orderItemList);

			// 消費税と合計金額の計算
			order.setOrderItemList(orderItemList);
			int totalPrice = order.getCalcTotalPrice();
			int tax = order.getTax();
			model.addAttribute("totalPrice", totalPrice);
			model.addAttribute("tax", tax);

			// DB orderにuserId・status・totalPriceのみをインサート(商品を追加したときはtotalPriceのみ変わる)
			order = orderService.findOrder(userId);
			if (order == null) {
				orderService.insert(userId, totalPrice);
			} else {
				Integer orderId = order.getId();
				orderService.upDate(totalPrice, orderId);
			}

			// DB order_Itemsに注文商品をインサート
			Order newOrder = orderService.findOrder(userId);
			newOrderItem.setOrderId(newOrder.getId());
			orderItemService.insert(newOrderItem);

			return "cart_list";
		}
	}
	
	/**
	 * ショッピングカートの中身を削除する
	 * 
	 * @param id　該当商品のid
	 * @param model 
	 * @return DBとsessionから削除してからショッピングカートを表示する画面へ遷移する。
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/delete")
	public String delete(Integer id, Model model) {
		// 引数のidと一致するorder_itemsのDBのデータを削除
		orderItemService.delete(id);

		// 引数のidと orderItemのidが一致すれば、OrderItemListから削除
		List<OrderItem> orderItemList = (List<OrderItem>) session.getAttribute("orderItemList");
		for (OrderItem orderItem : orderItemList) {
			if (orderItem.getId().equals(id)) {
				orderItemList.remove(orderItem);
				break;
			}
		}

		// orderItemListに値が入っていなければ、「カートに商品がありません」と表示。入っていれば、sessionスコープに詰めなおす。
		if (orderItemList.isEmpty()) {
			model.addAttribute("nullCartMessage", "カートに商品がありません");
		} else {
			session.setAttribute("orderItemList", orderItemList);
		}

		// 合計金額の計算
		Order order = new Order();
		order.setOrderItemList(orderItemList);
		int totalPrice = order.getCalcTotalPrice();
		int tax = order.getTax();
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("tax", tax);

		return "cart_list";
	}

}
