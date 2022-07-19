package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.domain.Item;
import com.example.form.ItemSearchForm;
import com.example.repository.ItemRepository;

class ItemServiceTest {

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private ItemService itemService;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
	}

	/**
	 * findByIdのテストメソッド。 <br>
	 * 
	 * @author makarasu
	 */
	@Test
	@DisplayName("findByIdのテスト")
	void findById() {
		// itemRepository.findById()が呼び出されたら、getItemId1()の値を返すよう定義
		when(itemRepository.findById(1)).thenReturn(getItemId1());

		// itemService.findByIdを呼び出し、結果をitemに格納
		Item item = itemService.findById(1);

		assertAll(() -> {
			// verify:モック化したメソッドの呼び出し回数を検証する
			// times()が1の時は、「itemRepositoryが1回だけ実行されたか確認する」となる
			verify(itemRepository, times(1)).findById(1);

			// itemの値とgetItemId1()の値が等しくなっているか検証。
			// usingRecursiveComparisonを付与することで、入れ子になっているクラスの検証でインスタンス比較にならないようにする
			assertThat(getItemId1()).usingRecursiveComparison().isEqualTo(item);
		});
	}

	/**
	 * findByName(あいまい検索)のテスト
	 * 
	 * @author makarasu
	 */
	@Test
	@DisplayName("findByNameのテスト")
	void findByName() {
		ItemSearchForm form = new ItemSearchForm();
		form.setName("赤");
		when(itemRepository.findByName(form, 1)).thenReturn(getItemsByName());

		List<Item> itemList = itemService.findByName(form, 1);

		assertAll(() -> {
			verify(itemRepository, times(1)).findByName(form, 1);
			assertThat(getItemsByName()).usingRecursiveComparison().isEqualTo(itemList);
		});
	}

	/**
	 * 単品データ注入用
	 * 
	 * @return 注入用データ（1商品分）
	 * @author makarasu
	 */
	public Item getItemId1() {
		Item item = new Item();
		item.setId(1);
		item.setName("浴衣（ネイビー）フラワー柄 朝顔");
		item.setDescription("ネイビーを基調に朝顔の柄が入った女性用浴衣です。涼しげな雰囲気にまとまります。大人っぽく着こなしたい方におススメです。");
		item.setPrice(7500);
		item.setImagePath("1.jpg");
		item.setSize("フリー");
		item.setKana("ゆかた（ねいびー）ふらわーがら あさがお");
		return item;
	}

	/**
	 * あいまい検索結果注入（赤、で検索した場合の結果）
	 * 
	 * @return
	 */
	public List<Item> getItemsByName() {
		List<Item> itemList = new ArrayList<>();
		Item item1 = new Item();
		item1.setId(29);
		item1.setName("帯（赤）フラワー 椿");
		item1.setDescription("赤が基調の花柄の帯です。浴衣コーデのアクセントにいかがですか？");
		item1.setPrice(1800);
		item1.setImagePath("29.jpg");
		item1.setSize("フリー");
		item1.setKana("おび（あか）ふらわー");

		Item item2 = new Item();
		item2.setId(19);
		item2.setName("帯（赤）");
		item2.setDescription("赤が基調のかわいい帯です。どんな色の浴衣にも合わせやすい一品です。");
		item2.setPrice(1000);
		item2.setImagePath("19.jpg");
		item2.setSize("フリー");
		item2.setKana("おび（あか）");

		Item item3 = new Item();
		item3.setId(39);
		item3.setName("浴衣（赤）フラワー柄 桜");
		item3.setDescription("赤を基調にさくら柄が入った女性用浴衣です。大人っぽい雰囲気にまとめたい方におススメです。");
		item3.setPrice(3480);
		item3.setImagePath("39.jpg");
		item3.setSize("フリー");
		item3.setKana("ゆかた（あか）ふらわーがら さくら");

		Item item4 = new Item();
		item4.setId(44);
		item4.setName("浴衣（赤）和柄 折り鶴");
		item4.setDescription("白を基調に折り鶴柄が入った女性用浴衣です。大人っぽい雰囲気にまとめたい方におススメです。");
		item4.setPrice(5280);
		item4.setImagePath("44.jpg");
		item4.setSize("フリー");
		item4.setKana("ゆかた（あか）わがら おりづる");

		Item item5 = new Item();
		item5.setId(17);
		item5.setName("浴衣（赤）フラワー柄 菊");
		item5.setDescription("赤を基調に菊の柄が入った女性用浴衣です。涼しげな雰囲気にまとまります。大人っぽく着こなしたい方におススメです。");
		item5.setPrice(3650);
		item5.setImagePath("17.jpg");
		item5.setSize("フリー");
		item5.setKana("ゆかた（あか）ふらわーがら きく");

		itemList.add(item1);
		itemList.add(item2);
		itemList.add(item3);
		itemList.add(item4);
		itemList.add(item5);

		return itemList;
	}

}
