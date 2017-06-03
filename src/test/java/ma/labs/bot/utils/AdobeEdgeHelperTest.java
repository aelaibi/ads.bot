package ma.labs.bot.utils;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 30/08/2016.
 */
public class AdobeEdgeHelperTest {

    @Test
    public void execute2(){
        String src2 = "<html><head>\n" +
                "	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\">\n" +
                "	<meta name=\"ad.size\" content=\"width=300,height=250\">\n" +
                "	<title>300x250 (Quaker- Good Start)</title>\n" +
                "	<script src=\"https://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/300x250_edge.js\"></script><script src=\"https://s0.2mdn.net/ads/studio/Enabler.js\"></script>\n" +
                "<!--Adobe Edge Runtime-->\n" +
                "    <script type=\"text/javascript\" charset=\"utf-8\" src=\"https://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/edge_includes/edge.6.0.0.min.js\"></script>\n" +
                "    <style>\n" +
                "        .edgeLoad-EDGE-17993826 { visibility:hidden; }\n" +
                "    </style>\n" +
                "<script>\n" +
                "   AdobeEdgeHelper.loadComposition('300x250', 'EDGE-17993826', {\n" +
                "    scaleToFit: \"none\",\n" +
                "    centerStage: \"none\",\n" +
                "    minW: \"0px\",\n" +
                "    maxW: \"undefined\",\n" +
                "    width: \"300px\",\n" +
                "    height: \"250px\"\n" +
                "}, {\"dom\":{}}, {\"dom\":{}});\n" +
                "</script>\n" +
                "<!--Adobe Edge Runtime End-->\n" +
                "<script type=\"text/javascript\">\n" +
                "	var clickTag = \"http://www.quakerarabia.com/ar/quaker-good-start\";\n" +
                "</script>\n" +
                "<script type=\"text/javascript\">\n" +
                "    \n" +
                "      (function() {\n" +
                "        var relegateNavigation = '';\n" +
                "        var handleClickTagMessage = function(e) {\n" +
                "          try {\n" +
                "            var eventData = JSON.parse(e.data);\n" +
                "          } catch (err) {\n" +
                "            return;\n" +
                "          }\n" +
                "          if (eventData.isInitClickTag) {\n" +
                "            if (eventData.clickTags) {\n" +
                "              for (var i = 0; i < eventData.clickTags.length; i++) {\n" +
                "                var clkTag = eventData.clickTags[i];\n" +
                "                window[clkTag.name] = clkTag.url;\n" +
                "              }\n" +
                "            } else if (eventData.clickTag) {\n" +
                "              window.clickTag = eventData.clickTag;\n" +
                "            }\n" +
                "            relegateNavigation = eventData.relegateNavigation;\n" +
                "          }\n" +
                "        };\n" +
                "\n" +
                "        if (open.call) {\n" +
                "          window.open = function(open) {\n" +
                "            return function(url, name, features) {\n" +
                "              if (relegateNavigation === 'parent') {\n" +
                "                var message = {'clickTag': url, 'isPostClickTag': true};\n" +
                "                parent.postMessage(JSON.stringify(message), '*');\n" +
                "              } else {\n" +
                "                var args = [url, name];\n" +
                "                if (features) {\n" +
                "                  args.push(features);\n" +
                "                }\n" +
                "                open.apply(window, args);\n" +
                "              }\n" +
                "            };\n" +
                "          }(window.open);\n" +
                "        }\n" +
                "\n" +
                "        if (window.addEventListener) {\n" +
                "          window.addEventListener(\n" +
                "              'message', handleClickTagMessage, false);\n" +
                "        } else {\n" +
                "          window.attachEvent('onmessage', handleClickTagMessage);\n" +
                "        }\n" +
                "      })();\n" +
                "    \n" +
                "  </script></head>\n" +
                "<body style=\"margin:0;padding:0;\">\n" +
                "	<div id=\"Stage\" class=\"EDGE-17993826\" style=\"position: relative; height: 250px; width: 300px; z-index: 0; right: auto; bottom: auto; overflow: hidden; text-overflow: clip; background-color: rgb(255, 255, 255); background-size: 100% 100%;\">\n" +
                "	<div id=\"Stage_bg\" class=\"Stage_bg_id\" style=\"position: absolute; margin: 0px; left: 0px; top: 0px; width: 300px; height: 250px; right: auto; bottom: auto; clip: rect(0px 300px 250px 0px); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); transform: translateZ(0px); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/bg.jpg&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_too_busy\" class=\"Stage_too_busy_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 249px; height: 26px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(-34px, 109px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/too_busy.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_cranberries\" class=\"Stage_cranberries_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 115px; height: 76px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(146px, 66px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/cranberries.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_wheat\" class=\"Stage_wheat_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 100px; height: 78px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(64px, 56px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/wheat.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_oats\" class=\"Stage_oats_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 101px; height: 88px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(-18px, 50px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/oats.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_product\" class=\"Stage_product_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 300px; height: 250px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(0px, 0px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/product.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_start_right\" class=\"Stage_start_right_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 199px; height: 27px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(61px, 24px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/start_right.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_good_start\" class=\"Stage_good_start_id\" style=\"position: absolute; margin: 0px; opacity: 1; left: 0px; top: 0px; width: 159px; height: 30px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(106px, 18px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/good_start.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_try_a_pack\" class=\"Stage_try_a_pack_id\" style=\"position: absolute; margin: 0px; opacity: 1; left: 0px; top: 0px; width: 167px; height: 43px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(75px, 54px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/try_a_pack.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_pro_box\" class=\"Stage_pro_box_id\" style=\"position: absolute; margin: 0px; opacity: 1; left: 0px; top: 0px; width: 300px; height: 250px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(0px, 0px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/pro_box.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_cta\" class=\"Stage_cta_id\" style=\"position: absolute; margin: 0px; opacity: 1; left: 0px; top: 0px; width: 159px; height: 32px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(71px, 214px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/cta.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_logo\" class=\"Stage_logo_id\" style=\"position: absolute; margin: 0px; opacity: 1; left: 0px; top: 0px; width: 55px; height: 69px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(21px, 0px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/images/logo.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_border\" class=\"Stage_border_id\" style=\"position: absolute; margin: 0px; left: 0px; top: 0px; width: 298px; height: 248px; right: auto; bottom: auto; border: 1px solid rgb(255, 177, 0); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); transform: translateZ(0px); background-color: rgba(192, 192, 192, 0); background-size: 100% 100%;\"></div><div id=\"Stage_clickTag\" class=\"Stage_clickTag_id\" style=\"position: absolute; margin: 0px; left: 0px; top: 0px; width: 300px; height: 250px; right: auto; bottom: auto; border: 0px solid rgb(102, 102, 102); cursor: pointer; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); transform: translateZ(0px); background-color: rgba(255, 255, 255, 0); background-size: 100% 100%;\"></div></div>\n" +
                "\n" +
                "</body></html>";
        String url2 = "https://s0.2mdn.net/2606317/1465734725704/AR_300x250_New/300x250.html";
        save("out2.html", AdobeEdgeHelper.execute(src2, url2));
    }
    @Test
    public void execute() throws Exception {
        String src1 = "<html><head>\n" +
                "	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "	<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\">\n" +
                "	<meta name=\"ad.size\" content=\"width=160,height=600\">\n" +
                "	<title>160x600 - Quaker Good Start</title>\n" +
                "	<script src=\"https://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/160x600_edge.js\"></script><script src=\"https://s0.2mdn.net/ads/studio/Enabler.js\"></script>\n" +
                "<!--Adobe Edge Runtime-->\n" +
                "    <script type=\"text/javascript\" charset=\"utf-8\" src=\"https://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/edge_includes/edge.6.0.0.min.js\"></script>\n" +
                "    <style>\n" +
                "        .edgeLoad-EDGE-86973749 { visibility:hidden; }\n" +
                "    </style>\n" +
                "<script>\n" +
                "   AdobeEdgeHelper.loadComposition('160x600', 'EDGE-86973749', {\n" +
                "    scaleToFit: \"none\",\n" +
                "    centerStage: \"none\",\n" +
                "    minW: \"0px\",\n" +
                "    maxW: \"undefined\",\n" +
                "    width: \"160px\",\n" +
                "    height: \"600px\"\n" +
                "}, {\"dom\":{}}, {\"dom\":{}});\n" +
                "</script>\n" +
                "<!--Adobe Edge Runtime End-->\n" +
                "<script type=\"text/javascript\">\n" +
                "	var clickTag = \"http://www.quakerarabia.com/ar/quaker-good-start\";\n" +
                "</script>\n" +
                "<script type=\"text/javascript\">\n" +
                "    \n" +
                "      (function() {\n" +
                "        var relegateNavigation = '';\n" +
                "        var handleClickTagMessage = function(e) {\n" +
                "          try {\n" +
                "            var eventData = JSON.parse(e.data);\n" +
                "          } catch (err) {\n" +
                "            return;\n" +
                "          }\n" +
                "          if (eventData.isInitClickTag) {\n" +
                "            if (eventData.clickTags) {\n" +
                "              for (var i = 0; i < eventData.clickTags.length; i++) {\n" +
                "                var clkTag = eventData.clickTags[i];\n" +
                "                window[clkTag.name] = clkTag.url;\n" +
                "              }\n" +
                "            } else if (eventData.clickTag) {\n" +
                "              window.clickTag = eventData.clickTag;\n" +
                "            }\n" +
                "            relegateNavigation = eventData.relegateNavigation;\n" +
                "          }\n" +
                "        };\n" +
                "\n" +
                "        if (open.call) {\n" +
                "          window.open = function(open) {\n" +
                "            return function(url, name, features) {\n" +
                "              if (relegateNavigation === 'parent') {\n" +
                "                var message = {'clickTag': url, 'isPostClickTag': true};\n" +
                "                parent.postMessage(JSON.stringify(message), '*');\n" +
                "              } else {\n" +
                "                var args = [url, name];\n" +
                "                if (features) {\n" +
                "                  args.push(features);\n" +
                "                }\n" +
                "                open.apply(window, args);\n" +
                "              }\n" +
                "            };\n" +
                "          }(window.open);\n" +
                "        }\n" +
                "\n" +
                "        if (window.addEventListener) {\n" +
                "          window.addEventListener(\n" +
                "              'message', handleClickTagMessage, false);\n" +
                "        } else {\n" +
                "          window.attachEvent('onmessage', handleClickTagMessage);\n" +
                "        }\n" +
                "      })();\n" +
                "    \n" +
                "  </script></head>\n" +
                "<body style=\"margin:0;padding:0;\">\n" +
                "	<div id=\"Stage\" class=\"EDGE-86973749\" style=\"position: relative; height: 600px; width: 160px; z-index: 0; right: auto; bottom: auto; overflow: hidden; text-overflow: clip; background-color: rgb(255, 255, 255); background-size: 100% 100%;\">\n" +
                "	<div id=\"Stage_bg\" class=\"Stage_bg_id\" style=\"position: absolute; margin: 0px; left: 0px; top: 0px; width: 160px; height: 600px; right: auto; bottom: auto; clip: rect(0px 160px 600px 0px); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); transform: translateZ(0px); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/bg.jpg&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_logo\" class=\"Stage_logo_id\" style=\"position: absolute; margin: 0px; opacity: 1; left: 0px; top: 0px; width: 55px; height: 74px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(20px, 0px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/logo.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_too_busy\" class=\"Stage_too_busy_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 143px; height: 111px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(9px, 245px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/too_busy.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_start_right\" class=\"Stage_start_right_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 143px; height: 63px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(28px, 144px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/start_right.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_cranberries\" class=\"Stage_cranberries_id\" style=\"position: absolute; margin: 0px; opacity: 0.005399; left: 0px; top: 0px; width: 154px; height: 102px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(-9px, 322px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/cranberries.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_wheat\" class=\"Stage_wheat_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 122px; height: 94px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(-4px, 253px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/wheat.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_oats\" class=\"Stage_oats_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 99px; height: 86px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(0px, 205px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/oats.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_product\" class=\"Stage_product_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 224px; height: 108px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(-33px, 410px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/product.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_pro_box\" class=\"Stage_pro_box_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 74px; height: 37px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(45px, 302px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/pro_box.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_try_a_pack\" class=\"Stage_try_a_pack_id\" style=\"position: absolute; margin: 0px; opacity: 1; left: 0px; top: 0px; width: 148px; height: 77px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(6px, 203.04px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/try_a_pack.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_cta\" class=\"Stage_cta_id\" style=\"position: absolute; margin: 0px; opacity: 0; left: 0px; top: 0px; width: 148px; height: 30px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(6px, 448px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/cta.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_good_start\" class=\"Stage_good_start_id\" style=\"position: absolute; margin: 0px; opacity: 1; left: 0px; top: 0px; width: 134px; height: 70px; right: auto; bottom: auto; transform-style: preserve-3d; transform: translate(12px, 127px) translateZ(0px) rotate(0deg) scale(1, 1); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); background-image: url(&quot;http://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/images/good_start.png&quot;); background-color: rgba(0, 0, 0, 0); background-size: 100% 100%; background-position: 0px 0px; background-repeat: no-repeat;\"></div><div id=\"Stage_border\" class=\"Stage_border_id\" style=\"position: absolute; margin: 0px; left: 0px; top: 0px; width: 158px; height: 598px; right: auto; bottom: auto; border: 1px solid rgb(255, 177, 0); -webkit-tap-highlight-color: rgba(0, 0, 0, 0); transform: translateZ(0px); background-color: rgba(192, 192, 192, 0); background-size: 100% 100%;\"></div><div id=\"Stage_clickTag\" class=\"Stage_clickTag_id\" style=\"position: absolute; margin: 0px; left: 0px; top: 0px; width: 160px; height: 600px; right: auto; bottom: auto; border: 0px solid rgb(102, 102, 102); cursor: pointer; -webkit-tap-highlight-color: rgba(0, 0, 0, 0); transform: translateZ(0px); background-color: rgba(255, 255, 255, 0); background-size: 100% 100%;\"></div></div>\n" +
                "\n" +
                "</body></html>";
        String url1 = "https://s0.2mdn.net/2606317/1465734721867/AR_160x600_New/160x600.html";
        save("out1.html", AdobeEdgeHelper.execute(src1, url1));
    }


    private static void save(String fileName, String content) {
        File file = new File("target/"+fileName);
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(AdobeEdgeHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}