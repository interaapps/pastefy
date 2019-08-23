@template(("header", ["title"=>"Homepage"]))!

<div id="web_contents">
<form method="POST" action="/create:paste">
		<input autocomplete="off" class="titleinput" type="text" name="title" placeholder="Filename (title) (Optional)">
		<textarea class="contentinput" name="content" id="input_contents" placeholder="Content"></textarea><br>
		<a onclick="$('#optional_paste_options').show();" style="cursor: pointer; color: #FFFFFF;">Optional Options</a><br><br>
		    <div id="optional_paste_options">
		        <input class="titleinput" autocomplete="new-password" type="password" id="password_input" name="password" placeholder="Password (optional)">
				@if(( (\app\classes\User::usingIaAuth()) ? \app\classes\User::loggedIn() : false ))#
					<a id="folderLoadingIndicator"></a>
					<br>
					<br>Folder:<br><br> <select id="folderList" name="folder">
						<option value="">/</option>
					</select>
				@endif
            </div>
		<br>
		<input class="submitbutton" type="submit" name="sub" value="Publish">
        <a class="clear" onclick="document.getElementById('input_contents').value = '';">Clear</a>
	</form>
</div>
<script src="/assets/js/homepage.js"></script>

@if(( (\app\classes\User::usingIaAuth()) ? \app\classes\User::loggedIn() : false ))#
	<script>
		$("#folderLoadingIndicator").html("<br>Loading the folders, please wait...<br>");
		Cajax.post("/get:folder").then((resp)=>{
			responseJson = JSON.parse(resp.responseText);
			$("#folderLoadingIndicator").html("");
			for (obj in responseJson) {
				var option = new Option(responseJson[obj], obj);
				$(option).html(responseJson[obj]);
				$("#folderList").append(option);
				if (window.location.hash=="#"+obj)
					$("#folderList").val(obj);
			}

		}).catch(()=>{
			$("#folderLoadingIndicator").html("Couldn't load the folder!");
		}).send();
	</script>
@endif

@template(("footer"))!