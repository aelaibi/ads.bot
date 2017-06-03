var pathsData = {
    'paths': []
};
var list = document.getElementsByTagName("path");

for (i = 0; i < list.length; i++) {
    pathsData['paths'].push(list[i].getAttribute("d"));
}
return JSON.stringify(pathsData);