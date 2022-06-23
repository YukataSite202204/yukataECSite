/**
 *選択数量に合わせて金額表示変更
 *@author makarasu
 */
'use strict'
$(function(){
	$('#selectCount').ready(function(){
		let price = $('#hiddenPrice').text()
		let sum = $('#selectCount').val() * price
		let intax = $('#selectCount').val() * price * 1.1
		$('#selectIntaxPrice').text(intax.toLocaleString() + "円（税込）")
		$('#selectSumPrice').text(sum.toLocaleString() + "円（税抜）" )
	});
	$('#selectCount').on('change', function(){
		let price = $('#hiddenPrice').text()
		let sum = $('#selectCount').val() * price
		let intax = $('#selectCount').val() * price * 1.1
		$('#selectIntaxPrice').text(intax.toLocaleString() + "円（税込）")
		$('#selectSumPrice').text(sum.toLocaleString() + "円（税抜）" )
	});
});

