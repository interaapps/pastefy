<form method="POST" action="/create:paste">
	<input autocomplete="off" class="titleinput" type="text" name="title" placeholder="Filename (title) (Optional)">
	<textarea class="contentinput" name="content" id="{{$cp_id}}_input" placeholder="Content"></textarea><br>
	<a id="{{$cp_id}}_open_optional_paste_options" style="cursor: pointer; color: #FFFFFF;">Optional Options</a><br><br>
		<div id="{{$cp_id}}_optional_paste_options">
			<input class="titleinput" autocomplete="new-password" type="password" id="password_input" name="password" placeholder="Password (optional)">
			@if(( (\app\classes\User::usingIaAuth()) ? \app\classes\User::loggedIn() : false ))#
				<a id="{{$cp_id}}_folderLoadingIndicator"></a>
				<br>
				<br>Folder:<br><br> <select id="{{$cp_id}}_folderList" name="folder">
					<option value="">/</option>
				</select>
			@endif
		</div>
	<br>
	<input class="submitbutton" type="submit" name="sub" value="Publish">
	<a class="clear" id="{{$cp_id}}_clear">Clear</a>
</form>

<script src="/assets/js/homepage.js"></script>

<script>
(function(id){
	@if(( (\app\classes\User::usingIaAuth()) ? \app\classes\User::loggedIn() : false ))#
		$("#"+id+"_folderLoadingIndicator").html("<br>Loading the folders, please wait...<br>");
		Cajax.post("/get:folder").then((resp)=>{
			responseJson = JSON.parse(resp.responseText);
			$("#"+id+"_folderLoadingIndicator").html("");
			for (obj in responseJson) {
				var option = new Option(responseJson[obj], obj);
				$(option).html(responseJson[obj]);
				$("#"+id+"_folderList").append(option);
				if (window.location.hash=="#"+obj)
					$("#"+id+"_folderList").val(obj);
			}

		}).catch(()=>{
			$("#"+id+"_folderLoadingIndicator").html("Couldn't load the folder!");
		}).send();
	@endif

	var textarea = $("#"+id+"_input").getFirstElement(); 
	textarea.onkeydown = function(event) {
		
		//support tab on textarea
		if (event.keyCode == 9) { //tab was pressed
			var newCaretPosition;
			newCaretPosition = textarea.getCaretPosition() + "    ".length;
			textarea.value = textarea.value.substring(0, textarea.getCaretPosition()) + "    " + textarea.value.substring(textarea.getCaretPosition(), textarea.value.length);
			textarea.setCaretPosition(newCaretPosition);
			return false;
		}
		if(event.keyCode == 8){ //backspace
			if (textarea.value.substring(textarea.getCaretPosition() - 4, textarea.getCaretPosition()) == "    ") { //it's a tab space
				var newCaretPosition;
				newCaretPosition = textarea.getCaretPosition() - 3;
				textarea.value = textarea.value.substring(0, textarea.getCaretPosition() - 3) + textarea.value.substring(textarea.getCaretPosition(), textarea.value.length);
				textarea.setCaretPosition(newCaretPosition);
			}
		}
		if(event.keyCode == 37){ //left arrow
			var newCaretPosition;
			if (textarea.value.substring(textarea.getCaretPosition() - 4, textarea.getCaretPosition()) == "    ") { //it's a tab space
				newCaretPosition = textarea.getCaretPosition() - 3;
				textarea.setCaretPosition(newCaretPosition);
			}    
		}
		if(event.keyCode == 39){ //right arrow
			var newCaretPosition;
			if (textarea.value.substring(textarea.getCaretPosition() + 4, textarea.getCaretPosition()) == "    ") { //it's a tab space
				newCaretPosition = textarea.getCaretPosition() + 3;
				textarea.setCaretPosition(newCaretPosition);
			}
		} 
	}

	$(document).ready(function () {
		$("#"+id+"_password_input").val("");
	});

	$("#"+id+"_optional_paste_options").hide();

	$("#"+id+"_open_optional_paste_options").click(function(){
		$("#"+id+"_optional_paste_options").show();	
	});

	$("#"+id+"_clear").click(function(){
		$("#"+id+"_input").val("");
	});

})("{{$cp_id}}");


</script>