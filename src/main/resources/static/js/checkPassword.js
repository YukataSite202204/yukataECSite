"use strict"

/*新規会員登録時のパスワード確認用Ajax*/

$(function(){
	$(document).on("keyup", "#checkPassword, #password", function(){
		let hostUrl = "http://localhost:8080/user/checkPassword";
		let inputPassword = $("#password").val();
		let checkInputPassword = $("#checkPassword").val();
		$.ajax({
			url:hostUrl,
			type:"post",
			dataType:"json",
			data:{
				password:inputPassword,
				checkPassword:checkInputPassword
			},
			async:true,
		}).done(function(data){
			$("#checkPasswordMessage").text(data.checkPasswordMessage);
			if((data.checkPasswordMessage)=="パスワードが一致しました"){
				console.log(checkInputPassword);
				$("#register_admin_btn").prop("disabled", false);
			}else{
				$("#register_admin_btn").prop("disabled", true);
			}
		}).fail(function(XMLHttpRequest, textStatus, errorThrown){
			console.log("XMLHttpRequest" + XMLHttpRequest);
			console.log("textStatus" + textStatus);
			console.log("errorThrown" + errorThrown);
		});
	});
});
