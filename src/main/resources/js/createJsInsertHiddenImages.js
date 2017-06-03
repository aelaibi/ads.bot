var manifest = lib.properties.manifest;
for (var i=0, l=manifest.length; i<l; i++) {
    var id = manifest[i].id;
    var elem = document.createElement("img");
    elem.setAttribute("src",  manifest[i].src);
    elem.setAttribute("id",  manifest[i].id);
    elem.setAttribute("style", "display:none");
    elem.setAttribute("alt",  manifest[i].id);
    document.body.appendChild(elem);
    var img = document.getElementById(id);
}
