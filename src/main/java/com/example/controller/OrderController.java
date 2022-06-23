package com.example.controller;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.domain.Item;
import com.example.domain.Order;
import com.example.domain.OrderHistory;
import com.example.domain.OrderItem;
import com.example.domain.User;
import com.example.form.OrderForm;
import com.example.service.OrderService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

/**
 * @author uehara
 */

@Controller
@RequestMapping("/order")
public class OrderController {

	@ModelAttribute
	public OrderForm setForm() {
		return new OrderForm();
	}

	@Autowired
	private OrderService service;

	@Autowired
	private HttpSession session;

	private final MailSender mailSender;

	public OrderController(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * 注文内容確認画面を表示させるメソッド
	 * 
	 * @param model
	 * @return 注文内容確認画面
	 */
	@RequestMapping("/show")
	public String showOrder(Model model) {

		User user = (User) session.getAttribute("user");
		if (user == null) {
			model.addAttribute("value", "order");
			return "forward:/user/tologin";
		}

		// ユーザーIDで注文検索
		Order order = service.findOrderById(user.getId());
		if (order == null) {
			return "order_confirm2";
		}

		@SuppressWarnings("unchecked")
		List<OrderItem> orderItemList = (List<OrderItem>) session.getAttribute("orderItemList");

		order.setOrderItemList(orderItemList);

		int tax = order.getTax();
		int total = order.getCalcTotalPrice();

		// 注文確認画面表示のためrequestスコープにセット
		model.addAttribute("order", order);
		model.addAttribute("total", total);
		model.addAttribute("tax", tax);

		return "order_confirm";
	}

	/**
	 * 配達日時が今から３時間後になっているか確認するメソッド
	 * 
	 * @param delivaryDate 配達日
	 * @param deliveryTime 配達時間
	 * @return 満たしていない場合はfalse
	 */
	@SuppressWarnings("deprecation")
	public boolean checkDeliveryTime(String deliveryDate, String deliveryTime) {

		// 以下5行で今の時間の３時間後の時間を取得
		Date nowDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(nowDate);
		calendar.add(Calendar.HOUR_OF_DAY, 3);
		nowDate = calendar.getTime();

		// 今の時間と選択された時間を比較 try-catchはdate型変換用
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		try {
			date = dateFormat.parse(deliveryDate);
			int hour = Integer.parseInt(deliveryTime);
			date.setHours(hour);
			return date.after(nowDate);

		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 注文を確定させるメソッド
	 * 
	 * @param orderForm 注文フォーム
	 * @param result
	 * @param model
	 * @param userId
	 * @return 注文完了画面にリダイレクト
	 * 
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping("/order")
	public String order(@Validated OrderForm orderForm, BindingResult result, Model model, String userId,
//			@RequestParam("stripeToken") 
			String stripeToken,
//			@RequestParam("stripeTokenType")
			String stripeTokenType,
//			@RequestParam("stripeEmail")
			String stripeEmail) {

		if (result.hasErrors()) {
			return showOrder(model);
		}

		// 配達日と配達時間の両方が選択されていた場合、いまより3時間後の時間になっているか確認
		if (orderForm.getDeliveryDate() != null && orderForm.getDeliveryTime() != null) {
			if (!(checkDeliveryTime(orderForm.getDeliveryDate(), orderForm.getDeliveryTime()))) {
				String s = "配達日時は現在から３時間後以降の日時を選択してください";
				model.addAttribute("deliveryTImeError", s);
				return showOrder(model);
			}
		}

		User user = (User) session.getAttribute("user");


		Order order = service.findOrderById(user.getId());

		BeanUtils.copyProperties(orderForm, order);

		// orderitemListセット
		@SuppressWarnings("unchecked")
		List<OrderItem> orderItemList = (List<OrderItem>) session.getAttribute("orderItemList");
		order.setOrderItemList(orderItemList);

		// 支払い方法セット
		order.setPaymentMethod(Integer.parseInt(orderForm.getPaymentMethod()));

		// 注文日セット
		Date today = new Date();
		order.setOrderDate(today);

		// 配達日時セット
		String s = orderForm.getDeliveryDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		try {
			date = dateFormat.parse(s);
			int hour = Integer.parseInt(orderForm.getDeliveryTime());
			date.setHours(hour);
			Timestamp ts = new Timestamp(date.getTime());
			order.setDeliveryTime(ts);
		} catch (ParseException e) {
			e.printStackTrace();
			return "order_confirm2";
		}

		// 支払い方法によってステータス変更
		Integer paymentMethod = Integer.parseInt(orderForm.getPaymentMethod());
		if (paymentMethod == 1) {
			order.setStatus(1);
		} else if (paymentMethod == 2) {
			order.setStatus(2);

			// 以下stripe APIに関する記述
			Stripe.apiKey = "sk_test_51L7U0DJUEdTQpHI8mBEiaHfA8iUUbqnMndP2dXZ3TRhtjP0UV472yEFu3RWD1r9pguEJFrNlpCCJQXC3XcJMuh6Z00AjI46n8i";
			Integer price = order.getTotalPrice();

			Map<String, Object> chargeMap = new HashMap<String, Object>();
			chargeMap.put("amount", price);
			chargeMap.put("description", "合計金額");
			chargeMap.put("currency", "jpy");
			chargeMap.put("source", stripeToken);

			try {
				Charge charge = Charge.create(chargeMap);
			} catch (StripeException e) {
				e.printStackTrace();
			}
			@SuppressWarnings({ "rawtypes", "unused" })
			ResponseEntity response = ResponseEntity.ok().build();
		}

		// orderテーブルにupdate
		service.updateOrder(order);

		// メール送信メソッド呼び出し
		sendEmail(orderForm.getDestinationEmail());

		// orderHistoryに値をセットしてDBにINSERT
		List<OrderItem> list = order.getOrderItemList();
		for (OrderItem item : list) {
			OrderHistory orderHistory = new OrderHistory();
			orderHistory.setOrderId(order.getId());
			orderHistory.setUserId(user.getId());
			orderHistory.setStatus(order.getStatus());
			orderHistory.setOrderDate(order.getOrderDate());
			orderHistory.setItemId(item.getItemId());
			orderHistory.setQuantity(item.getQuantity());
			Item i = item.getItem();
			orderHistory.setPrice(i.getPrice());
			orderHistory.setImagePath(i.getImagePath());
			orderHistory.setItemName(i.getName());
			// ここで1件ずつInsert
			service.addOrderHistory(orderHistory);
		}
		session.removeAttribute("orderItemList");
		
		return "redirect:/order/orderRedirect";
	}

	/**
	 * リダイレクト用メソッド
	 */
	@RequestMapping("/orderRedirect")
	public String orderRedirect() {
		return "order_finished";
	}

	/**
	 * 注文履歴画面を表示させるメソッド
	 * 
	 * @param model
	 * @return 注文履歴画面
	 * 
	 */
	@RequestMapping("/toHistory")
	public String toOrderHistory(Model model) {

		User user = (User) session.getAttribute("user"); // 現在のユーザー取り出す

		// ログインしていない状態で注文履歴画面URL直遷移しようとされた場合、ログインを求めるようにする
		if (user == null) {
			model.addAttribute("loginErrorFromCart", "ログインしてください");
			return "forward:/user/tologin";
		}

		List<OrderHistory> orderHistoryList = service.getOrderHistory(user.getId());

		if (orderHistoryList.size() == 0)
			return "order_history2";

		model.addAttribute("orderHistory", orderHistoryList);

		return "order_history";
	}

	/**
	 * トップ画面に戻るメソッド
	 * 
	 * @return トップ画面
	 */
	@RequestMapping("/toTop")
	public String toTop() {
		return "top";
	}

	/**
	 * 注文確認メールを送るメソッド
	 * 
	 * @param mailAddress メールアドレス
	 * 
	 */
	public void sendEmail(String mailAddress) {

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom("rakuraku.yukata@gmail.com");
		mailMessage.setTo(mailAddress);

		mailMessage.setSubject("注文確認メール");
		mailMessage.setText(
				"この度はらくらく浴衣をご利用いただき、\r\nありがとうございます。\r\n注文が確定いたしました。\r\n注文は注文履歴よりご確認いただけます。\r\nまたのご利用をお待ちしております。\r\nらくらく浴衣(000-0000-0000)");

		try {
			mailSender.send(mailMessage);
		} catch (MailException e) {
			e.printStackTrace();
		}
	}

}
