eUrl = "";

window.onclick = function(e) {
   console.log(document.activeElement.href);
   e.preventDefault();
   if(document.activeElement.hasAttribute('href')){
       eUrl = document.activeElement.href;
   }
   return document.activeElement.href;
};

window.open = function(open) {
    return function(url, name, features) {
        console.log(url);
        eUrl = url;
        return url;
    };
}(window.open);



arguments[0].focus();
arguments[0].click();
/*
var tmp = document.activeElement.href;
console.log(document.activeElement.href);
console.log(tmp);*/
return document.activeElement.href;
/*
if (tmp != "") {

} else {
    arguments[0].click();
    return eUrl;
}*/