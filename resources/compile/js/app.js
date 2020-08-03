// Function to copy contents
function copyStringToClipboard(str) {
    // Create new element
    var el = document.createElement('textarea');
    // Set value (string to be copied)
    el.value = str;
    // Set non-editable to avoid focus and move outside of view
    el.setAttribute('readonly', '');
    el.style = {position: 'absolute', left: '-9999px'};
    document.body.appendChild(el);
    // Select text inside element
    el.select();
    // Copy text to clipboard
    document.execCommand('copy');
    document.body.removeChild(el);
 }
 

  let snackBarTimeout;
  
  function showSnackBar(text, color="#17fc2e", background="#222530") {
    const snackbar = document.querySelector('#snackbar');
    snackbar.textContent = text;
    snackbar.style.color = color;
    snackbar.style.backgroundColor = background;
    snackbar.classList.add('show');
    clearTimeout(snackBarTimeout);
    snackBarTimeout = setTimeout(() => {
      snackbar.classList.remove('show');
    }, 1500);
  }
 
 function openNav() {
     //if (screen.width <= 720) {
         document.getElementById("navbar").style.width = "100%";
     /*} else {
         document.getElementById("navbar").style.width = "250px";
     }*/
 }
 
 function closeNav() {
     document.getElementById("navbar").style.width = "0px";
 }
 
 $(document).ready(function(){
    let s = document.createElement('div');
    s.id = 'snackbar';
    s.textContent = 'done';
    document.body.append(s);


    $("#short_new_paste").click(function(e){
        e.stopPropagation();
    });

    $(window).click(function(e){
        if (window.innerWidth >= 720 && e.target.id != "create_new_paste") {
            if ($("#short_new_paste").css("display") == "block"){
                event.preventDefault();
                $("#short_new_paste").css("display", "none");
            }
        }
    });

    $(window).keydown(function(e){
        if (e.key == "Escape" && $("#short_new_paste").css("display") == "block")
            $("#short_new_paste").css("display", "none");
        else if (e.key == "N" && window.location.pathname != "/" && $("#short_new_paste").css("display") == "none") // Open the new paste window
            $("#short_new_paste").css("display", "block");
    });

    $("#create_new_paste").click(function(e){
        if (window.innerWidth >= 720) {
            if ($("#short_new_paste").css("display") == "none")
                $("#short_new_paste").css("display", "block");
            else
                $("#short_new_paste").css("display", "none");
            e.preventDefault();
        }
    });

 });

 HTMLTextAreaElement.prototype.getCaretPosition = function () { //return the caret position of the textarea
    return this.selectionStart;
};
HTMLTextAreaElement.prototype.setCaretPosition = function (position) { //change the caret position of the textarea
    this.selectionStart = position;
    this.selectionEnd = position;
    this.focus();
};
HTMLTextAreaElement.prototype.hasSelection = function () { //if the textarea has selection then return true
    if (this.selectionStart == this.selectionEnd) {
        return false;
    } else {
        return true;
    }
};
HTMLTextAreaElement.prototype.getSelectedText = function () { //return the selection text
    return this.value.substring(this.selectionStart, this.selectionEnd);
};
HTMLTextAreaElement.prototype.setSelection = function (start, end) { //change the selection area of the textarea
    this.selectionStart = start;
    this.selectionEnd = end;
    this.focus();
};