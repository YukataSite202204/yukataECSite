package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.domain.User;
import com.example.form.UserForm;
import com.example.service.UserService;

/**
 * @author mikami
 *
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommandLineRunner {

	@Autowired
	private UserService userService;

	@Autowired
	private HttpSession session;

	@ModelAttribute
	public UserForm userSetUpForm() {
		return new UserForm();
	}

	/**
	 * 新規会員登録画面に遷移する.
	 * 
	 * @return 新規会員登録画面
	 */
	@RequestMapping("/registryform")
	public String registryform() {
		return "register_admin";
	}

	/**
	 * 会員登録内容が問題ないかチェックする.<br>
	 * ①バリデーションチェック<br>
	 * ②メールアドレス重複チェック<br>
	 * 
	 * @param userForm
	 * @param result
	 * @param model
	 * @return 問題なければ、会員登録確認画面へ遷移する
	 */
	@RequestMapping("registryConfirmation")
	public String registryConfirmation(@Validated UserForm userForm, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "register_admin";
		}
		Integer emailFlg = userService.findByEmail(userForm.getEmail());
		if (emailFlg.equals(1)) {
			model.addAttribute("emailDuplicateError", "こちらのメールアドレスでは登録できません");
			return "register_admin";
		}
		model.addAttribute("registrationDetails", userForm);
		return "register_admin_confirmation";
	}

	/**
	 * 新規会員登録ボタンを押下した時のメソッド. <br>
	 * 問題なく登録できれば、ログイン画面に遷移させる
	 * 
	 * @param userForm
	 * @param result
	 * @return String
	 */

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub

	}

	@RequestMapping("/registry")
	public String registry(@Validated UserForm userForm, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "register_admin";
		}

		// パスワードをハッシュ化してDBにいれる
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodeedPassword = passwordEncoder.encode(userForm.getPassword());

		// フォームをドメインにコピー
		User user = new User();
		BeanUtils.copyProperties(userForm, user);
		user.setPassword(encodeedPassword);

		// 姓名を合わせて1個の名前にする
		String firstName = userForm.getFirstName();
		String lastName = userForm.getLastName();
		String name = lastName.concat(firstName);
		user.setName(name);

		// 新規会員登録を行う
		// すでに登録されているメールアドレスを登録しようとした際はエラー表示
		try {
			userService.insert(user);
		} catch (DuplicateKeyException e) {
			model.addAttribute("emailDuplicateError", "そのメールアドレスはすでに使われています");
			return "register_admin";
		}

		return "redirect:/user/registrationComplete";

	}

	/**
	 * 登録完了画面に遷移させるためだけのメソッド.<br>
	 * 新規会員登録時、ブラウザの更新やF5で再度インサート文が流れないようにするために実装しています。
	 * 
	 * @return 登録完了画面
	 */
	@RequestMapping("/registrationComplete")
	public String registrationComplete() {
		return "register_admin_complete";
	}

	/**
	 * ログイン画面に遷移する
	 * 
	 * @return String
	 */
	@RequestMapping("/tologin")
	public String tologin(HttpServletRequest request, Model model) {

		// 注文確認画面から遷移していた場合order、そうでなければotherをrequestスコープにセット
		if (request.getAttribute("value") != null) {
			model.addAttribute("orderSet", "order");
		} else {
			model.addAttribute("orderSet", "other");
		}

		return "login";
	}

	/**
	 * ログイン判定
	 * 
	 * @return String
	 */
	@RequestMapping("/login")
	public String login(UserForm userForm, Model model, String status) {
		// ログインフォームからのuserFormから、メールアドレスとパスワードを取り出す
		String email = userForm.getEmail();
		String password = userForm.getPassword();

		// DBのハッシュ化したパスワードとログイン時に入力されるパスワードが一致するかどうか調べる。
		BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
		User user = userService.findByEmailHash(email);

		if (user == null) {
			model.addAttribute("loginError", "メールアドレスかパスワードが異なります");
			return "login";
		} else if (bcpe.matches(password, user.getPassword())) {
			System.out.println("ハッシュ化と一致");
		} else {
			model.addAttribute("loginError", "メールアドレスかパスワードが異なります");
			return "login";
		}

		// DBに一致があった場合、ユーザー情報をセッションに入れ、item_listに飛ばす。
		session.setAttribute("user", user);

		// 注文確認画面からのログインの場合はカートに、その他は商品一覧に。
		if (status.equals("order")) {
			return "forward:/shoppingCart/inCart";
		} else {
			return "forward:/yukata/list";

		}
	}

	/**
	 * ログアウトする<br>
	 * ログアウトしたら、Top画面に遷移させる。
	 * 
	 * @return String
	 */
	@RequestMapping("logout")
	public String registeredCheck() {
		session.removeAttribute("user");
		return "logout";
	}

	/**
	 * 新規登録時のメールアドレス確認用メソッド.
	 * 
	 * @param password：パスワード
	 * @param checkPassword：確認用パスワード
	 * @return パスワードが一致したかどうかのメッセージ
	 */
	@ResponseBody
	@RequestMapping(value = "/checkPassword", method = RequestMethod.POST)
	public Map<String, String> check(String password, String checkPassword) {
		Map<String, String> map = new HashMap<>();
		String checkPasswordMessage = null;
		if (checkPassword == null || checkPassword.isBlank()) {
			checkPasswordMessage = "確認用パスワードを入力してください";
		} else if (password.equals(checkPassword)) {
			checkPasswordMessage = "パスワードが一致しました";
		} else {
			checkPasswordMessage = "パスワードが一致していません";
		}
		map.put("checkPasswordMessage", checkPasswordMessage);
		return map;
	}

}
