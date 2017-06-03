var imgWraping = document.evaluate('//video/following-sibling::div/img/..', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
var iWraping = document.evaluate('//video/following-sibling::i', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
var video = document.getElementsByTagName('video')[0];
var hideAllAndPlayVideo = function(){
	imgWraping.style.display='none';
	iWraping.style.display='none';
	video.play();
};
var showAllElements = function(){
	iWraping.style.display='block';
};
imgWraping.addEventListener("click", hideAllAndPlayVideo, true);
iWraping.addEventListener("click", hideAllAndPlayVideo, true);
video.addEventListener("pause", showAllElements, true);
video.addEventListener("play", hideAllAndPlayVideo, true);