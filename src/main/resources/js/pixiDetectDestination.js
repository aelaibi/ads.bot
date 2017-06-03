var pixiAllA   = document.getElementsByTagName("a");
var pixiUrl    = "";
if(pixiAllA != undefined && pixiAllA != null && pixiAllA.length !=0){
	var pixiFirstA = pixiAllA[0];
	function aClick(e, elem){
		e.preventDefault();
		pixiUrl = proceedpixiUrl(elem.href);
		if(pixiUrl == undefined || pixiUrl == null || pixiUrl == ""){
			if(elem.hasAttribute("onclick")){
				eval(elem.getAttribute("onclick"));
			}
		}
	}
	function proceedpixiUrl(u){
		if(u == undefined || u == null || u == "") {
			return "";
		}
		if(u.startsWith('javascript')){
			var pixiMatches = u.match('javascript:(.*)');
			var pixiInterest= pixiMatches[1];
			if(pixiInterest.startsWith('window.open')){
				return proceedpixiUrl(pixiInterest);
			} else {
				return eval(pixiInterest);
			}
		} else if(u.startsWith('window.open')){
			var pixiMatches = u.match('window\.open\((.*)\)');
			var pixiInterest= pixiMatches[1];
			try{
				var returnVal = eval(pixiInterest);
				return returnVal;
			}catch(ex){
			 	if(ex instanceof ReferenceError){
			 		return pixiInterest;
				} else {
					logErreurs(e);
				}
			}
		} else if(u.startsWith('//')){
			return window.location.protocol + u;
		} else {
			return u;
		}
	}
	window.onclick = function(e) {
	    e.preventDefault();
	    aClick(e, pixiFirstA);
	    return false;
	};
	window.open = function(open) {
	    return function(u, name, features) {
	        pixiUrl = proceedpixiUrl(u);
	        return pixiUrl;
	    };
	}(window.open);
	pixiFirstA.click();
}
return pixiUrl;