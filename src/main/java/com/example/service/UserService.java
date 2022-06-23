package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.domain.User;
import com.example.repository.UserRepository;

/**
 * usersテーブルに関する処理を行うサービスです.
 * 
 * @author mikami
 *
 */
@Service
public class UserService {

	/**
	 * userRepositoryをDIします.
	 */
	@Autowired
	private UserRepository userRepository;

	/**
	 * ユーザを新規登録します.
	 * 
	 * @param user (新規登録したいユーザー情報一式）<br>
	 * @return User (新規登録したユーザー情報一式（自動採番されたidも取得できるように実装））<br>
	 */
	public User insert(User user) {
		return userRepository.insert(user);
	}
	
	/**
	 * ログイン機能
	 * 
	 * @param email
	 * @param password
	 * @return User
	 */
	public User findByEmailAndPassword(String email, String password) {
		return userRepository.findByEmailAndPassword(email, password);
	}

	/*
	 * ハッシュ化した際のログイン機能
	 */
	public User findByEmailHash(String email) {
		return userRepository.findByEmailHash(email);
	}
	/**
	 * すでに登録されているＥｍａｉｌかどうか調べる
	 * 
	 * @param email
	 * @return すでに登録されている場合：1
	 * @return 登録されていない場合：0
	 */
	public Integer findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

}
