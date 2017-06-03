/*CREER UN ATTRIBUT PIXI POUR FLAGER TOUS LES ELEMENTS PRESENTS LORS DE L EXTRACTION*/
var pixiElements = document.getElementsByTagName("*");
for (var i=0; i<pixiElements.length; i++){
	pixiElements[i].setAttribute('pixi', 'true');
}