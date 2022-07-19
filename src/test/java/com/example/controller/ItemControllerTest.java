package com.example.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.domain.Item;
import com.example.form.ItemSearchForm;
import com.example.service.ItemService;

/**
 * ItemControllerのテストクラス
 * 
 * @author makarasu
 *
 */
@SpringBootTest
class ItemControllerTest {

	private MockMvc mockMvc;

	@Autowired
	WebApplicationContext wac;

	@Autowired
	ItemService itemService;

	@BeforeEach
	void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	@DisplayName("トップページ遷移メソッドのテスト")
	void topTest() throws Exception {
		mockMvc.perform(post("/yukata/top")).andExpect(status().isOk()).andExpect(view().name("top"));
	}

	@Test
	@DisplayName("商品一覧ページ遷移メソッドテスト")
	void indexTest() throws Exception {

		mockMvc.perform(post("/yukata/list"))
				.andExpect(status().isOk()).andExpect(view().name("item_list"))
				// sessionスコープに（key,value）と等しいものが存在するか確認
				.andExpect(request().sessionAttribute("calledSource", "index"))
				.andExpect(request().sessionAttribute("nameList", nameList()))
				.andExpect(request().sessionAttribute("kanaList", kanaList()))
				// sessionスコープに"name"、"message"が存在しないか確認（removeされているか確認）
				.andExpect(request().sessionAttributeDoesNotExist("name"))
				.andExpect(request().sessionAttributeDoesNotExist("message"));
	}

	@Test
	@DisplayName("あいまい検索メソッドテスト")
	void searchTest() throws Exception {
		// .param("name","赤")で、formのnameに値を入れる。
		mockMvc.perform(get("/yukata/search").param("name", "赤")).andExpect(status().isOk())
				.andExpect(view().name("item_list"))
				.andExpect(request().sessionAttribute("calledSource", "search"))
				.andExpect(request().sessionAttribute("name", "赤"));
	}

	@Test
	@DisplayName("並び替えメソッドテスト")
	void changeOrderTest() throws Exception {
		mockMvc.perform(get("/yukata/order").param("name", "赤").param("order", "2")).andExpect(status().isOk())
				.andExpect(view().name("item_list")).andExpect(request().sessionAttribute("calledSource", "order"))
				.andExpect(request().sessionAttribute("name", "赤")).andExpect(request().sessionAttribute("order", 2));
	}

	@Test
	@DisplayName("商品詳細ページ遷移メソッドテスト")
	void detailTest() throws Exception {

		// "/yukata/detail"(商品詳細ページのメソッド)をgetで呼び出す
		MvcResult result = mockMvc.perform(get("/yukata/detail").param("id", "20"))
				// HTTPステータスコードが200であることを確認
				.andExpect(status().isOk())
				// modelに"item"が存在するか確認
				.andExpect(model().attributeExists("item"))
				// 返されるview(HTML名)が"item_detail"であることを確認
				.andExpect(view().name("item_detail")).andReturn();
		// 上記テスト結果からmodelに格納された"item"の値を取得
		Item itemResult = (Item) result.getModelAndView().getModel().get("item");

		// 想定値と一致するかテストする。（クラス型が異なるため.toString()をつけてテスト実施）
		assertEquals(findByIdItem().toString(), itemResult.toString());
	}

	@Nested
	@DisplayName("商品一覧ページのページングテスト")
	class indexPagingtests {

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("初回")
		void indexPagingTest1() throws Exception {
			Integer boxSizeMax = 4;
			Integer intNowPage = 1;
			List<Item> itemList = itemService.findAll(intNowPage);
			MvcResult result = mockMvc.perform(get("/yukata/page").param("calledSource", "index"))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxSizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxSizeMax)))
					.andExpect(request().attribute("nowPage", 1)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> resultList = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemList.toString(), resultList.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「次へ」クリック")
		void indexPagingTest2() throws Exception {
			Integer intNowPage = 2;
			MvcResult result = mockMvc.perform(
					get("/yukata/page").param("nowPage", "1").param("boxFlg", "1").param("calledSource", "index")
							.param("nextBtnFlg", "1")
							.sessionAttr("boxSizeMax", 4).sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();
			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findAll(intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「前へ」クリック")
		void indexPagingTest3() throws Exception {
			Integer intNowPage = 1;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("nowPage", "2").param("boxFlg", "1")
							.param("calledSource", "index").param("beforeBtnFlg", "1").sessionAttr("boxSizeMax", 4)
							.sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();
			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findAll(intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「ボックスボタン」クリック")
		void indexPagingTest4() throws Exception {
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("nowPage", "1").param("boxFlg", "1")
							.param("calledSource", "index").param("boxNumber", "2").sessionAttr("boxSizeMax", 4)
							.sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();
			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findAll(intNowPage).toString(), itemListResult.toString());
		}
	}

	@Nested
	@DisplayName("（検索該当あり）あいまい検索ページングテスト")
	class searchPagingTests {
		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("初回")
		void searchPagingTest10() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("赤");
			Integer boxSizeMax = 1;
			Integer intNowPage = 1;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "search").param("name", form.getName()))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxSizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxSizeMax)))
					.andExpect(model().attributeExists("itemCount"))
					.andExpect(
							request().sessionAttribute("message", "検索結果：" + itemService.countOfFindByName(form) + "件"))
					.andExpect(model().attributeExists("itemList")).andExpect(model().attribute("nowPage", intNowPage))
					.andExpect(model().attribute("boxFlg", "1")).andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(getItemsByName().toString(), itemListResult.toString());
			Integer itemCountResult = (Integer) result.getModelAndView().getModel().get("itemCount");
			assertEquals(5, itemCountResult);
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「次へ」クリック")
		void searchPagingTest11() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("浴衣");
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("nowPage", "1").param("boxFlg", "1")
							.param("calledSource", "search").param("nextBtnFlg", "1")
							.sessionAttr("name", form.getName())
							.sessionAttr("boxSizeMax", 4).sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();
			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findByName(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「前へ」クリック")
		void searchPagingTest12() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("浴衣");
			Integer intNowPage = 1;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("nowPage", "2").param("boxFlg", "1")
							.param("calledSource", "search").param("beforeBtnFlg", "1")
							.sessionAttr("name", form.getName())
							.sessionAttr("boxSizeMax", 4).sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();
			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findByName(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「ボックスボタン」クリック")
		void searchPagingTest13() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("浴衣");
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("nowPage", "1").param("boxFlg", "1")
							.param("calledSource", "search").param("boxNumber", "2").sessionAttr("name", form.getName())
							.sessionAttr("boxSizeMax", 4).sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();
			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findByName(form, intNowPage).toString(), itemListResult.toString());
		}
	}

	@Nested
	@DisplayName("（検索該当なし）あいまい検索ページングテスト")
	class noHitSearchPagingTest {
		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("初回")
		void searchPagingtest3() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("紫陽花");
			Integer boxsizeMax = 4;
			Integer intNowPage = 1;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "search").param("name", form.getName()))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(request().sessionAttribute("message", "条件にマッチする商品はありません。"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListresult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findAll(intNowPage).toString(), itemListresult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「次へ」クリック")
		void searchPagingTest2() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("紫陽花");
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "search").param("name", form.getName())
							.param("boxFlg", "1").param("nowPage", "1").param("nextBtnFlg", "1")
							.sessionAttr("message", "条件にマッチする商品はありません。").sessionAttr("boxSizeMax", 4)
							.sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(request().sessionAttribute("message", "条件にマッチする商品はありません。"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListresult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findAll(intNowPage).toString(), itemListresult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「前へ」クリック")
		void searchPagingTest3() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("紫陽花");
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "search").param("name", form.getName())
							.param("boxFlg", "1").param("nowPage", "3").param("beforeBtnFlg", "1")
							.sessionAttr("message", "条件にマッチする商品はありません。").sessionAttr("boxSizeMax", 4)
							.sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(request().sessionAttribute("message", "条件にマッチする商品はありません。"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListresult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findAll(intNowPage).toString(), itemListresult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「ボックスボタン」クリック")
		void searchPagingTest4() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("紫陽花");
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "search").param("name", form.getName())
							.param("boxFlg", "1").param("nowPage", "3").param("boxNumber", "2")
							.sessionAttr("message", "条件にマッチする商品はありません。").sessionAttr("boxSizeMax", 4)
							.sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(request().sessionAttribute("message", "条件にマッチする商品はありません。"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListresult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findAll(intNowPage).toString(), itemListresult.toString());
		}
	}

	@Nested
	@DisplayName("（空文字検索）あいまい検索ページングテスト")
	class noWordSearchPagingTest {

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("初回")
		void searchPagingTest20() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			// 何も入力しないで検索する場合、空文字がformに渡されるため空文字を渡す
			form.setName("");
			Integer boxsizeMax = 4;
			Integer intNowPage = 1;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "search").param("name", form.getName()))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemCount"))
					.andExpect(
							request().sessionAttribute("message", "検索結果：40件"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findByName(form, intNowPage).toString(), itemListResult.toString());

			Integer itemCountResult = (Integer) result.getModelAndView().getModel().get("itemCount");
			assertEquals(40, itemCountResult);
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「次へ」クリック")
		void searchPagingTest21() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("");
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "search").param("name", form.getName())
							.param("boxFlg", "1").param("nowPage", "1")
							.param("nextBtnFlg", "1").sessionAttr("message", "検索結果：40件")
							.sessionAttr("boxSizeMax", 4)
							.sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findByName(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「前へ」クリック")
		void searchPagingTest22() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("");
			Integer boxsizeMax = 4;
			Integer intNowPage = 1;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "search").param("name", form.getName())
							.param("nowPage", "2")
							.param("boxFlg", "1").param("beforeBtnFlg", "2").sessionAttr("message", "検索結果：40件")
							.sessionAttr("boxSizeMax", 4).sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findByName(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「ボックスボタン」クリック")
		void searchPagingTest23() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("");
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "search").param("name", form.getName())
							.param("nowPage", "1").param("boxFlg", "1").param("boxNumber", "2")
							.sessionAttr("message", "検索結果：40件").sessionAttr("boxSizeMax", 4)
							.sessionAttr("boxSizeList", boxSizeList(4)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.findByName(form, intNowPage).toString(), itemListResult.toString());
		}
	}

	@Nested
	@DisplayName("（検索なし・空文字検索）並べ替えページングテスト")
	class changeOrderTest1 {
		// 値段が高い順に変更した場合でテスト実施

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("初回")
		void orderTest1() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("");
			form.setOrder(1);
			Integer intNowPage = 1;
			Integer boxSizeMax = 4;
			MvcResult result = mockMvc
					.perform(get("/yukata/order").param("name", form.getName()).param("order",
							form.getOrder().toString()))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("calledSource", "order"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxSizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxSizeMax)))
					.andExpect(model().attributeExists("itemList")).andExpect(model().attribute("nowPage", intNowPage))
					.andExpect(model().attribute("boxFlg", "1")).andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「次へ」クリック")
		void orderTest2() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("");
			form.setOrder(1);
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").param("name", form.getName())
							.param("order", form.getOrder().toString())
							.param("boxFlg", "1").param("nowPage", "1").param("nextBtnFlg", "1")
							.sessionAttr("boxSizeMax", boxsizeMax).sessionAttr("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「前へ」クリック")
		void orderTest3() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("");
			form.setOrder(1);
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").param("name", form.getName())
							.param("order", form.getOrder().toString()).param("boxFlg", "1").param("nowPage", "3")
							.param("beforeBtnFlg", "1").sessionAttr("boxSizeMax", boxsizeMax)
							.sessionAttr("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「ボックスボタン」クリック")
		void orderTest4() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("");
			form.setOrder(1);
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").sessionAttr("name", form.getName())
							.sessionAttr("order", form.getOrder()).param("boxFlg", "1").param("nowPage", "3")
							.param("boxNumber", "2").sessionAttr("boxSizeMax", boxsizeMax)
							.sessionAttr("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}
	}

	@Nested
	@DisplayName("（検索該当あり）並び替えページングテスト")
	class changeOrderTest2 {
		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("初回")
		void orderTest1() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("浴衣");
			form.setOrder(1);
			Integer intNowPage = 1;
			Integer boxSizeMax = 3;
			MvcResult result = mockMvc
					.perform(get("/yukata/order").param("name", form.getName()).param("order",
							form.getOrder().toString()))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("calledSource", "order"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxSizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxSizeMax)))
					.andExpect(model().attributeExists("itemList")).andExpect(model().attribute("nowPage", intNowPage))
					.andExpect(model().attribute("boxFlg", "1")).andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「次へ」クリック")
		void orderTest2() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("浴衣");
			form.setOrder(1);
			Integer boxsizeMax = 3;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").param("name", form.getName())
							.param("order", form.getOrder().toString()).param("boxFlg", "1").param("nowPage", "1")
							.param("nextBtnFlg", "1").sessionAttr("boxSizeMax", boxsizeMax)
							.sessionAttr("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「前へ」クリック")
		void orderTest3() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("");
			form.setOrder(1);
			Integer boxsizeMax = 4;
			Integer intNowPage = 1;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").param("name", form.getName())
							.param("order", form.getOrder().toString()).param("boxFlg", "1").param("nowPage", "2")
							.param("beforeBtnFlg", "1").sessionAttr("boxSizeMax", boxsizeMax)
							.sessionAttr("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「ボックスボタン」クリック")
		void orderTest4() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("浴衣");
			form.setOrder(1);
			Integer boxsizeMax = 3;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").sessionAttr("name", form.getName())
							.sessionAttr("order", form.getOrder()).param("boxFlg", "1").param("nowPage", "1")
							.param("boxNumber", "2").sessionAttr("boxSizeMax", boxsizeMax)
							.sessionAttr("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}
	}

	@Nested
	@DisplayName("（検索該当なし）並び替えページングテスト")
	class changeOrderTest3 {
		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("初回")
		void orderTest1() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("紫陽花");
			form.setOrder(1);
			Integer boxsizeMax = 4;
			Integer intNowPage = 1;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").sessionAttr("name", form.getName())
							.sessionAttr("order", form.getOrder()).sessionAttr("message", "条件にマッチする商品はありません。"))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(request().sessionAttribute("message", "条件にマッチする商品はありません。"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListresult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListresult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「次へ」クリック")
		void orderTest2() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("紫陽花");
			form.setOrder(1);
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").param("name", form.getName())
							.param("order", form.getOrder().toString()).param("boxFlg", "1").param("nowPage", "1")
							.param("nextBtnFlg", "1").sessionAttr("boxSizeMax", boxsizeMax)
							.sessionAttr("boxSizeList", boxSizeList(boxsizeMax))
							.sessionAttr("message", "条件にマッチする商品はありません。"))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(request().sessionAttribute("message", "条件にマッチする商品はありません。"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「前へ」クリック")
		void orderTest3() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("紫陽花");
			form.setOrder(1);
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").param("name", form.getName())
							.param("order", form.getOrder().toString()).param("boxFlg", "1").param("nowPage", "3")
							.param("beforeBtnFlg", "1").sessionAttr("boxSizeMax", boxsizeMax)
							.sessionAttr("boxSizeList", boxSizeList(boxsizeMax))
							.sessionAttr("message", "条件にマッチする商品はありません。"))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(request().sessionAttribute("message", "条件にマッチする商品はありません。"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("「ボックスボタン」クリック")
		void orderTest4() throws Exception {
			ItemSearchForm form = new ItemSearchForm();
			form.setName("紫陽花");
			form.setOrder(1);
			Integer boxsizeMax = 4;
			Integer intNowPage = 2;
			MvcResult result = mockMvc
					.perform(get("/yukata/page").param("calledSource", "order").sessionAttr("name", form.getName())
							.sessionAttr("order", form.getOrder()).param("boxFlg", "1").param("nowPage", "1")
							.param("boxNumber", "2").sessionAttr("boxSizeMax", boxsizeMax)
							.sessionAttr("boxSizeList", boxSizeList(boxsizeMax))
							.sessionAttr("message", "条件にマッチする商品はありません。"))
					.andExpect(status().isOk()).andExpect(view().name("item_list"))
					.andExpect(request().sessionAttribute("boxSizeMax", boxsizeMax))
					.andExpect(request().sessionAttribute("boxSizeList", boxSizeList(boxsizeMax)))
					.andExpect(request().sessionAttribute("message", "条件にマッチする商品はありません。"))
					.andExpect(model().attributeExists("itemList"))
					.andExpect(request().attribute("nowPage", intNowPage)).andExpect(request().attribute("boxFlg", "1"))
					.andReturn();

			List<Item> itemListResult = (List<Item>) result.getModelAndView().getModel().get("itemList");
			assertEquals(itemService.changeOrder(form, intNowPage).toString(), itemListResult.toString());
		}
	}

	/**
	 * 単品データ注入用
	 * 
	 * @return 注入用データ（1商品分）
	 * @author makarasu
	 */
	public Item findByIdItem() {
		Item item = new Item();
		item.setId(20);
		item.setName("帯（青）");
		item.setDescription("青が基調のクールな帯です。浴衣コーデを美しくまとめたい貴方におすすめです。");
		item.setPrice(1000);
		item.setImagePath("20.jpg");
		item.setDeleted(false);
		item.setSize("フリー");
		item.setKana("おび（あお）");
		return item;
	}

	/**
	 * オートコンプリート（商品名）データ注入用
	 * 
	 * @return 商品名リスト
	 * @author makarasu
	 */
	public List<String> nameList() {
		List<String> nameList = new ArrayList<>();
		String itemName = "浴衣（ネイビー）フラワー柄 朝顔,浴衣（青）フラワー柄 朝顔,浴衣（ピンク）フラワー柄 朝顔,"
				+ "浴衣（水色）フラワー柄 朝顔,浴衣（黄色）フラワー柄 朝顔,浴衣（黄緑）フラワー柄 朝顔,浴衣（グレー）フラワー柄 朝顔,浴衣（白）フラワー柄 朝顔,浴衣（黒）フラワー柄 朝顔,"
				+ "浴衣（青）フラワー柄 菊,浴衣（ピンク）フラワー柄 菊,浴衣（水色）フラワー柄 菊,浴衣（黄色）フラワー柄 菊,"
				+ "浴衣（黄緑）フラワー柄 菊,浴衣（ネイビー）フラワー柄 菊,浴衣（白）フラワー柄 菊,浴衣（赤）フラワー柄 菊,"
				+ "浴衣（黒）フラワー柄 菊,帯（赤）,帯（青）,下駄（24.0),下駄（25.0),男性用浴衣（白）,"
				+ "男性用浴衣（ネイビー）,帯（赤）フラワー 椿,帯（白）幾何学柄,帯（白）へこ帯,男性用浴衣（濃ネイビー）,"
				+ "浴衣（白）手毬柄,浴衣（白）フラワー柄 水仙,浴衣（白）幾何学柄,浴衣（ネイビー）フラワー柄 梅,"
				+ "浴衣（ネイビー）ストライプ柄,浴衣（グレー）フラワー柄 牡丹,浴衣（赤）フラワー柄 桜,浴衣（グレー）和柄 麻の葉,"
				+ "浴衣（青）フラワー柄 桜,浴衣（青）フラワー柄 桜,浴衣（ネイビー）フラワー柄 桜,浴衣（赤）和柄 折り鶴";
		String[] itemNames = itemName.split(",");
		for (String item : itemNames) {
			nameList.add(item);
		}
		return nameList;
	}

	/**
	 * オートコンプリート（商品名読み仮名）データ注入用
	 * 
	 * @return 商品名読み仮名リスト
	 * @author makarasu
	 */
	public List<String> kanaList() {
		List<String> kanaList = new ArrayList<>();
		String itemKana = "ゆかた（ねいびー）ふらわーがら あさがお,ゆかた（あお）ふらわーがら あさがお,"
				+ "ゆかた（ぴんく）ふらわーがら あさがお,ゆかた（みずいろ）ふらわーがら あさがお,ゆかた（きいろ）ふらわーがら あさがお,"
				+ "ゆかた（きみどり）ふらわーがら あさがお,ゆかた（ぐれー）ふらわーがら あさがお,ゆかた（しろ）ふらわーがら あさがお,"
				+ "ゆかた（くろ）ふらわーがら あさがお,ゆかた（あお）ふらわーがら きく,ゆかた（ぴんく）ふらわーがら きく,"
				+ "ゆかた（みずいろ）ふらわーがら きく,ゆかた（きいろ）ふらわーがら きく,ゆかた（きみどり）ふらわーがら きく,"
				+ "ゆかた（ねいびー）ふらわーがら きく,ゆかた（しろ）ふらわーがら きく,ゆかた（あか）ふらわーがら きく,"
				+ "ゆかた（くろ）ふらわーがら きく,おび（あか）,おび（あお）,げた（24.0）,げた（25.0）,だんせいようゆかた（しろ）,"
				+ "だんせいようゆかた（ねいびー）,おび（あか）ふらわー,おび（しろ）きかがくがら,おび（しろ）へこおび,"
				+ "だんせいようゆかた（のうねいびー）,ゆかた（しろ）てまりがら,ゆかた（しろ）ふらわーがら すいせん,ゆかた（しろ）きかがくがら,"
				+ "ゆかた（ねいびー）ふらわーがら うめ,ゆかた（ねいびー）すとらいぷがら,ゆかた（ぐれー）ふらわーがら ぼたん,"
				+ "ゆかた（あか）ふらわーがら さくら,ゆかた（ぐれー）わがら あさのは,ゆかた（あお）ふらわーがら さくら,"
				+ "ゆかた（あお）ふらわーがら さくら,ゆかた（ねいびー）ふらわーがら さくら,ゆかた（あか）わがら おりづる";
		String[] itemKanas = itemKana.split(",");
		for (String item : itemKanas) {
			kanaList.add(item);
		}
		return kanaList;
	}

	public List<Integer> boxSizeList(Integer boxSizeMax) {
		List<Integer> boxSizeList = new LinkedList<>();
		for (int i = 1; i <= boxSizeMax; i++) {
			boxSizeList.add(i);
		}
		return boxSizeList;
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

		itemList.add(item2);
		itemList.add(item1);
		itemList.add(item3);
		itemList.add(item5);
		itemList.add(item4);

		return itemList;
	}

}
