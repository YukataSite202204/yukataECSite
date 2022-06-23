package com.example.form;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;



/**
 * usersテーブルに対応するフォームです.
 * 
 * @author mikami
 *
 */

public class UserForm {

	/** id(自動採番） */
	private Integer id;

	/** 姓 */
	private String lastName;

	/** 名 */
	private String firstName;

	/** Eメールアドレス */
	@NotBlank(message = "メールアドレスを入力してください")
	@Email(message = "メールアドレスの形式が不正です")
	private String email;

	/** パスワード */
	@Size(min = 8, max = 16, message = "8文字以上16文字以内で設定してください")
	@Pattern(regexp = "^[a-zA-Z0-9._-]{0,}$", message = "半角数字、半角英字大文字・小文字、記号（._-） のみ使用できます")
	private String password;


	/** 郵便番号 */
	@NotBlank(message = "郵便番号を入力してください")
	@Pattern(regexp = "^[0-9]{3}-[0-9]{4}$", message = "郵便番号はXXX-XXXXの形式で入力してください")
	private String zipcode;

	/** 住所 */
	@NotBlank(message = "住所を入力して下さい")
	private String address;

	/** 電話番号 */
	@NotBlank(message = "電話番号を入力してください")
	@Pattern(regexp = "^[0-9]{2,3}-[0-9]{3,4}-[0-9]{3,4}$", message = "電話番号はXXX-XXXX-XXXXの形式で入力してください")
	private String telephone;

	/** 遷移先分岐のためのフィールド */
	private String status;


	public UserForm(
			Integer id, 
			String lastName, 
			String firstName,
			String email,
			String password,
			String zipcode,
			String address,
			String telephone,
			String status) {
		super();
		this.id = id;
		this.lastName = lastName;
		this.firstName = firstName;
		this.email = email;
		this.password = password;
		this.zipcode = zipcode;
		this.address = address;
		this.telephone = telephone;
		this.status = status;
	}
	
	public UserForm() {
		
	}


	@Override
	public String toString() {
		return "UserForm [id=" + id + ", lastName=" + lastName + ", firstName=" + firstName + ", email=" + email
				+ ", password=" + password + ", zipcode=" + zipcode + ", address="
				+ address + ", telephone=" + telephone + ", status=" + status + "]";
	}


	// 姓と名の相関バリデーションチェック
	@AssertTrue(message = "姓・名を入力して下さい")
	public boolean isNameValid() {
		if (lastName == null || lastName.isBlank()) {
			return false;
		} else if (firstName == null || firstName.isBlank()) {
			return false;
		}
		return true;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


}
