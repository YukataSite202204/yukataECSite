/**
 *オートコンプリート機能
 *@author makarasu
 */
$(function(){
	var nameList = $('#autoComplete1').text();
	var kanaList = $('#autoComplete2').text();
	//1文字列として取得したリストを分割する
	nameList = nameList.split(',')
	kanaList = kanaList.split(',');
	//リストの先頭・末尾に含まれる[]を削除する
	nameList[0] = nameList[0].replace('[' ,'');
	kanaList[0] = kanaList[0].replace('[', '');
	nameList[nameList.length - 1] = nameList[nameList.length - 1].replace(']' ,'');
	kanaList[nameList.length - 1] = kanaList[nameList.length - 1].replace(']' ,'');
	//余分な半角スペースを削除する
	$.each(nameList, function(index){
		nameList[index] = nameList[index].replace(' ','');
		kanaList[index] = kanaList[index].replace(' ','');
	})
	//整えた商品名リスト・読み仮名リストを連想配列に入れる
	var autoCompleteList = [nameList[0], kanaList[0]];
	$.each(nameList, function(index){
		autoCompleteList[index] = [kanaList[index], nameList[index]];
	})		
	//オートコンプリート機能の設定　ソースに連想配列、どちらかに部分一致するものがあれば商品名をリストに入れる
	$('.search-name-input').autocomplete({
		source: function(request, response){
			var re = new RegExp('(' + request.term + ')'),
				list =  [];				
			$.each(autoCompleteList, function(i, values){
				if(values[0].match(re) || values[1].match(re)){
					list.push(values[1]);
				}
			});
			response(list);
		},
		delay: 200
	});

	/** 
	 * 検索結果クリアボタンを押したとき、検索フォームごと検索結果のsessionを削除する
	 * @author makarasu
	 */
	$('#searchReset').on('click', function(){
		$('.search-name-input').remove();
	});
});

