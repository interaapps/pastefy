function copyStringToClipboard(str) {
    var el = document.createElement('textarea');
    el.value = str;
    el.setAttribute('readonly', '');
    el.style = {position: 'absolute', left: '-9999px'};
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
 }
 

let snackBarTimeout;

function showSnackBar(text="", color="#17fc2e", background="#222530") {
  const snackbar = document.getElementById('snackbar');
  snackbar.textContent = text;
  snackbar.style.color = color;
  snackbar.style.backgroundColor = background;
  snackbar.classList.add('show');
  clearTimeout(snackBarTimeout);
  snackBarTimeout = setTimeout(() => {
    snackbar.classList.remove('show');
  }, 2600);
}


export default {copyStringToClipboard, showSnackBar}