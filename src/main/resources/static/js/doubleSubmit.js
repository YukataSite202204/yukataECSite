/**
 * 新規会員登録時、登録ボタンを複数回押下されないようにするためのjsです
 */
 
 "use strict";
 
 $(function(){
	$("#registration-btn").on("click", function(){
		$("#registration-btn").prop("disabled", true);
		$("#registration-btn").closest('form').submit();
	});
});
