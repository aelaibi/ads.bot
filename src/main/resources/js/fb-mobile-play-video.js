var video = document.getElementsByTagName('video')[0];
video.removeAttribute('crossorigin');
var playImg = document.evaluate('//video/following-sibling::div', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
var poster = document.evaluate('//video/following-sibling::i', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
var hideElementsAndPlayVideo = function(){
	poster.style.display = 'none';
	playImg.style.display = 'none';
	video.style.display = 'block';
	video.play();
};
var showElements = function(){
	playImg.style.display = 'block';
};
if(playImg != null && playImg != undefined){
	playImg.style.zIndex = "1000";
	playImg.addEventListener("click", hideElementsAndPlayVideo, true);
}
if(poster != null && poster != undefined){
	poster.addEventListener("click", hideElementsAndPlayVideo, true);
}
video.addEventListener("play", hideElementsAndPlayVideo, true);
video.addEventListener("pause", showElements, true);