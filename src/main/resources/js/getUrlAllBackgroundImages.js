var all = document.getElementsByTagName('*');
var images = {
    'images': []
};
var img;
for (var i = 0, max = all.length; i < max; i++) {
    img = all[i];
    style = img.currentStyle || window.getComputedStyle(img, false), bi = style.backgroundImage.slice(5, -2);
    if (bi != '') {
        images['images'].push(bi);
        img.style.backgroundImage = style.backgroundImage;
    }
}
return JSON.stringify(images);