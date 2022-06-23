package com.example.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.domain.User;

/**
 * usersテーブルを操作するリポジトリです.
 * 
 * @author mikami
 *
 */
@Repository
public class UserRepository {

	/**
	 * テンプレートクラスをDIします.
	 * 
	 */
	@Autowired
	private NamedParameterJdbcTemplate template;

	/**
	 * usersテーブル用のRowMapperを作成します.
	 */
	private static final RowMapper<User> USER_ROW_MAPPER = (rs, i) -> {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setName(rs.getString("name"));
		user.setEmail(rs.getString("email"));
		user.setPassword(rs.getString("password"));
		user.setZipcode(rs.getString("zipcode"));
		user.setAddress(rs.getString("address"));
		user.setTelephone(rs.getString("telephone"));
		return user;
	};

	/**
	 * 集約関数を返すためのローマッパー.
	 */
	private static final RowMapper<Integer> AGGREGATE_FUNCTION_ROW_MAPPER = (rs, i) -> {
		Integer count = rs.getInt("count");
		return count;
	};

	/**
	 * ユーザを新規登録します.
	 * 
	 * @param user (新規登録したいユーザー情報一式）<br>
	 * @return User (新規登録したユーザー情報一式（自動採番されたidも取得できるように実装））<br>
	 */
	public User insert(User user) {
		String sql = "INSERT INTO users (name, email, password, zipcode, address, telephone)"
				+ " VALUES (:name, :email, :password, :zipcode, :address, :telephone)";
		SqlParameterSource param = new BeanPropertySqlParameterSource(user);

		KeyHolder keyHolder = new GeneratedKeyHolder();
		String[] keyColumnNames = { "id" };
		template.update(sql, param, keyHolder, keyColumnNames);
		user.setId(keyHolder.getKey().intValue());

		return user;
	}

	/**
	 * emailとpasswordから1人のユーザーを特定します。（ログイン機能）. <br>
	 * 一致がなければnullを戻す。
	 * 
	 * @param email
	 * @param password
	 * @return User
	 */
	public User findByEmailAndPassword(String email, String password) {
		String sql = "SELECT id, name, email, password, zipcode, address, telephone"
				+ " FROM users"
				+ " WHERE email=:email"
				+ " AND password=:password";
		SqlParameterSource param = new MapSqlParameterSource().addValue("email", email).addValue("password", password);
		List<User> userList = template.query(sql, param, USER_ROW_MAPPER);
		if (userList.size() == 0) {
			return null;
		}
		return userList.get(0);
	}

	/*
	 * ハッシュ化したときはメールアドレスのみの抽出。 パスワードが合致するかはControllerで判断します。（小渕）
	 */
	public User findByEmailHash(String email) {
		String sql = "SELECT id, name, email, password, zipcode, address, telephone FROM users"
				+ " WHERE email=:email";
		SqlParameterSource param = new MapSqlParameterSource().addValue("email", email);
		List<User> userList = template.query(sql, param, USER_ROW_MAPPER);
		if (userList.size() == 0) {
			return null;
		}
		return userList.get(0);
	}

	/**
	 * すでに登録されているＥｍａｉｌかどうか調べる
	 * 
	 * @param email
	 * @return すでに登録されている場合：1
	 * @return 登録されていない場合：0
	 */
	public Integer findByEmail(String email) {
		String sql = "SELECT count(*) FROM users WHERE email = :email";
		SqlParameterSource param = new MapSqlParameterSource().addValue("email", email);
		return template.queryForObject(sql, param, AGGREGATE_FUNCTION_ROW_MAPPER);
	}
}
