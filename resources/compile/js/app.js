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
 
 // Checks scroll to add Shadow to the navbar
 /*function checkScroll() {
     
     if (window.pageYOffset > 1) {
         nav.style.background = "rgba(51, 51, 68, 1)";
         nav.style.boxShadow = " 0px -38px 18px 34px rgba(0,0,0,0.55)";
     }
     else {
         nav.style.background = "rgba(51, 51, 68, 0.5)";
         nav.style.boxShadow = "0px 7px 17px -10px rgba(0,0,0,0)";
     }
 
 
 }
 $(document).ready(function() {
     var nav = $("#nav");
     var navmenu = document.getElementById("navmenu");
     checkScroll();
     window.onscroll = function() {
         checkScroll();
     };
 });
 */
 
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
 