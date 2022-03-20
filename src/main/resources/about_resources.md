# About Resources
This file will explain what the different resources are and where they come from.

## mapColors.json
Map color data contains the colors of various blocks when they appear on maps. Currently, the data I've pulled contains a block for every color except for the color corresponding to water.

The map color data is taken from [MapartCraft](https://rebane2001.com/mapartcraft/). The data for the selected blocks used to be pulled into a json file with the following script:

```javascript
function download(filename, text) {
var element = document.createElement('a');
element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
element.setAttribute('download', filename);

element.style.display = 'none';
document.body.appendChild(element);

element.click();

document.body.removeChild(element);
}

let mapdata = [];
for (let i=0; i<window.blocklist.length; i++) {
blockid = document.querySelector('input[name="color' + i + '"]:checked').value;
if (blockid == -1) continue;
let name = window.blocklist[i][1][blockid][0];
let data = window.blocklist[i][1][blockid][1];
mapdata.push({
	block: data ? `${name}[${data.replaceAll("'", "")}]` : name,
	colors: window.blocklist[i][0]
});
}

download ('mapColors.json', JSON.stringify(mapdata, null, 2));
```

But ever since they rewrote their site in react, it became impossible to extract the json from the Javascript console, so I wrote a new program that can extract it from their coloursJSON.json file here: [https://github.com/hhhzzzsss/MapartCraftPresetExtractor](https://github.com/hhhzzzsss/MapartCraftPresetExtractor).

## blocks.json, entities.json, language.json
These files are from [https://github.com/PrismarineJS/minecraft-data](https://github.com/PrismarineJS/minecraft-data).

## badapple/badapple.txt
The frames of a BadApple video playing at 10fps converted into a braille text format. The code that generated it is here: [BadAppleToBraille]

## badapple/badapple.mid
This is the music that plays alongside the braille BadApple video.(https://github.com/hhhzzzsss/BadAppleToBraille/blob/main/README.md).

## default-config.yml
The default config file.