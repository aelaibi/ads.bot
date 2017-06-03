var videoContainer = arguments[0];
var dataStore = JSON.parse(videoContainer.getAttribute('data-store'));
var src = dataStore.src.replace('\/','/');
var child = '<video class="_53mv" width="'+dataStore.width+'" height="'+dataStore.height+'" autobuffer="true" controls="" x-webkit-airplay="allow" playinfullscreen="false" src="'+src+'" style="background-color: rgb(0, 0, 0); bottom: 0px; cursor: pointer; font-size: 14px; height: 175px; left: 0px; line-height: 18px; list-style-type: none; max-width: 360px; perspective-origin: 180px 87.5px; position: absolute; right: 0px; text-align: left; top: 0px; transform-origin: 180px 87.5px 0px; width: 360px; word-wrap: break-word; z-index: 4; -moz-column-gap: 14px; display: none;"/>';
videoContainer.insertAdjacentHTML('afterbegin', child);