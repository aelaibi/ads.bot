function getAbsoluteElement(element){
	var PIXIelements = element.getElementsByTagName('*');

	var proceedElement = function(elem, topElement) {
		if (!elem) return "";
		var win = document.defaultView || window, style;
		var out = "";
		if (win.getComputedStyle) {
			style = win.getComputedStyle(elem, '');
			for (var i=0; i<style.length; i++) {
			    if(topElement && style[i].toUpperCase() == "DISPLAY"){
			        out += "display : block;";
			    } else {
			        if (style.getPropertyValue(style[i]) != PIXIgetDefaultByPropertyName(elem, style[i])) {
                        out += style[i] + ':' + style.getPropertyValue(style[i]) + ";";
                    }
			    }
			}
		}
		elem.style = out;
	};
	var PIXIgetDefaultByPropertyName = function(element, property){
        return window.getDefaultComputedStyle(element).getPropertyValue(property);
    };
	var proceedElementChildren = function(){
		for (var i = 0; i < PIXIelements.length; i++) {
			proceedElement(PIXIelements[i], false);
		}
	};
	/*var removeClasses = function(){
		element.className = '';
		for (var i = 0; i < PIXIelements.length; i++) { PIXIelements[i].className = ''; }
	};*/
	proceedElement(element, true);
	proceedElementChildren();
	/*removeClasses();*/
	return element.outerHTML;
}
return getAbsoluteElement(arguments[0]);