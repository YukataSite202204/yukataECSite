'use strict'

$(function(){
	$(document).on('click', '#get_address_btn', function(){
		if($('#zipcode').val().length < 8){
			alert('郵便番号はハイフンを入れて入力してください　（例）123-1234');
			exit;
		};
		$.ajax({
			url: 'https:/zipcoda.net/api',
			dataType: 'jsonp',
			data: {
				zipcode: $('#zipcode').val().replace('-','')
			},
			async: true
		}).done(function(data) {
			console.dir(JSON.stringify(data));
			console.log(data.items[0].components);
			/* 県名も取りたいなら下記 */
			$('#address').val(data.items[0].state_name + data.items[0].address);
			/* 県名は取りたくないなら下記 */
			/*$('#address').val(data.items[0].address);*/
		}).fail(function(XMLHttpRequest, textStatus, errorThrown) {
			alert('正しい番号を入力してください');
			console.log('XMLHttpRequest:' + XMLHttpRequest.status);
			console.log('textStatus:' + textStatus);
			console.log('errorThrown:' + errorThrown.message);
		});
	});

	/**
	 * 郵便番号で数値以外が入力された場合は住所検索ボタンが不活性化する。(テンキー入力未対応)
	 * @author makarasu
	  */
	$(document).on('keyup', '#zipcode', function(e){
  		let k = e.keyCode;
  		let str = String.fromCharCode(k);
  		//数値、十字キー（キーコード37～40）、バックスペース（キーコード：8）、デリート（キーコード：46）、ハイフン（キーコード：189）以外が入力された場合は住所検索ボタンが非活性化する
  		if(!(str.match(/[0-9]/)　|| (37 <= k && k <= 40) || k === 8 || k === 46 || k === 189)){
			$('#get_address_btn').prop('disabled', true);
		//数値が入力された場合でも、入力欄にハイフン以外の文字列が含まれている場合は非活性化したままにする
  		}else if(!($('#zipcode').val().match(/^[0-9\-]*$/))){
			$('#get_address_btn').prop('disabled', true);
		//上記以外（数値、ハイフンのみ入力の場合）は、住所検索ボタンが活性化する
		}else{
			$('#get_address_btn').removeAttr('disabled');
		}
	});
	
    //支払い方法によって表示するボタン
	$('input[name="paymentMethod"]:radio').change(function() {
		var value = $("input[name='paymentMethod']:checked").val();
		switch (value) {
			case "1":
				$('.creditPay-btn').hide();
				$('#order-btn').show();
				$("#order-btn").removeAttr("disabled");
				break;
			case "2":
				$('.creditPay-btn').show();
				$('#order-btn').hide();
				$("#order-btn").attr("disabled", "disabled");
				break;
		}
	});

	$('#order-btn').on('click', function(eve) {
		eve.preventDefault();
		$(this).attr("disabled", "disabled");
		$('#orderForm').submit();
	});

$("stripe-button-el").css("background-image", "linear-gradient(#EF9A9A,#EF9A9A);");
console.log($("stripe-button-el:hidden"));
});

let observer = new MutationObserver(function(mr) {
	console.log(JSON.stringify(mr));
	$("stripe-button-el").css("background-image", "linear-gradient(#EF9A9A,#EF9A9A);")
});

observer.observe(document.getElementById("creditPayBtn"), {
	childList: true,
	subtree: true
});

