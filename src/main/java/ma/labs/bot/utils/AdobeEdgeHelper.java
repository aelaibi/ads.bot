package ma.labs.bot.utils;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author labs003
 */
public class AdobeEdgeHelper {


    private static org.slf4j.Logger logger = LoggerFactory.getLogger(AdobeEdgeHelper.class);

    private static Pattern pattern;
    private static Matcher matcher;

    private static int getPubType(String html) {
        int type = -1;
        pattern = Pattern.compile("_edge.js");
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            type = 1;
        }
        pattern = Pattern.compile("_edgePreload.js");
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            type = 2;
        }
        return type;
    }

    private static String fixEdgeScript(String html, String frameUrl) {
        String rootUrl = frameUrl.substring(0, frameUrl.lastIndexOf("/")) + "/";
        String edgeScriptContent = loadFile(rootUrl + getProjectName(frameUrl) + "_edge.js");
        String _edgePattern = "(<script src=(\"|')(http.*_edge\\.js)(\"|')>( |)<\\/script>)";
        String replace = "<script type='text/javascript'>"
                + "pixiFixMargin = function(){document.getElementById('Stage').style.margin = 0;};\n"
                + "window.onload = pixiFixMargin;\n"
                + "pixiFixURL='" + rootUrl + "';\n"
                + "pixi_edge= function(){\n" + edgeScriptContent + "\n};</script>\n";
        pattern = Pattern.compile(_edgePattern);
        matcher = pattern.matcher(html);
        while (matcher.find()) {
            String found = matcher.group();
            html = html.substring(0, matcher.start()) + replace + html.substring(matcher.start() + found.length());
        }
        return html;
    }

    private static String fixEdgePluging(String html) {
        String _edgePluginPattern = "src=(\"|)(http.*edge.6.*?)(\"|')";
        pattern = Pattern.compile(_edgePluginPattern);
        matcher = pattern.matcher(html);
        while (matcher.find()) {
            String found = matcher.group();
            html = html.substring(0, matcher.start()) + " src=\"/scripts/pixi.edge.6.min.js\"" + html.substring(matcher.start() + found.length());
        }
        return html;
    }

    private static String fixSpecialCharacters(String html) {
        html = html.replaceAll("&lt;", "<");
        return html;
    }

    private static String loadFile(String stringUrl) {
        try {
            URL url = new URL(stringUrl);
            URLConnection conn = url.openConnection();
            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine + "\n");
            }
            return response.toString();
        } catch (MalformedURLException ex) {
            logger.error(null, ex);
        } catch (IOException ex) {
            logger.error(null, ex);
        }
        return null;
    }

    private static String getProjectName(String frameUrl) {
        String name = frameUrl.substring(frameUrl.lastIndexOf("/"));
        name = name.replaceAll("(/|.html)", "");
        if (name.equals("")) {
            return "index";
        } else {
            return name;
        }
    }



    public static String execute(String html, String frameUrl) {

        int pubType = getPubType(html);
        logger.debug("adobe egde ads type {}",pubType);
        String outputHTML = html;

        switch (pubType) {
            case 1:
                outputHTML = fixEdgeScript(outputHTML, frameUrl);
                outputHTML = fixEdgePluging(outputHTML);
                outputHTML = fixSpecialCharacters(outputHTML);

                break;
            case 2:
                outputHTML = fixExternalFiles(html, frameUrl);
                //logger.debug(html);
                break;
            default:
                logger.debug("Unknown adobe egde ads type");
                break;
        }
        return outputHTML;
    }

    private static String fixExternalFiles(String html, String frameUrl) {
        String rootUrl = frameUrl.substring(0, frameUrl.lastIndexOf("/")) + "/";
        String reg = "<script\\b[^>]*>([\\s\\S]*?)<\\/script>";
        int shiftPosition = 0;
        matcher = Pattern.compile(reg).matcher(html);
        while (matcher.find()) {
            if (matcher.group(0).contains("src")) {
                String found = matcher.group(0);
                logger.debug(found);
                String src = getScr(found);
                if(StringUtils.isBlank(src)){
                    continue;
                }
                String functionName = src.substring(src.lastIndexOf("/"));
                functionName = functionName.replaceAll("\"|'|\\/|(\\.min.js)|(\\.js)", "");
                functionName = functionName.replaceAll("(x)|(\\.)|(-)", "_");
                functionName = "_" + functionName;
                String functionCode = "";

                String scriptUrl = src.replaceAll("src|=|\"|'", "");
                //logger.debug("scriptUrl >" + scriptUrl);

                if (src.contains("_edgePreload.js")) {
                    //logger.debug("src.contains(\"_edgePreload.js\"");
                    String scriptfileContent = loadFile(scriptUrl);
                    String functionToOverride = "a.yepnope.injectJs=function(a,c,d,e,i,j){";
                    String replace = functionToOverride
                            + "\n console.log('eeeeee'); window['_'+a.substring(a.lastIndexOf('/'),a.length).replace(/(.min.js)|(.js)|(\\/)/g,'').replace(/(\\.)|(-)|(x)/g,'_')](null);\n";
                    scriptfileContent = scriptfileContent.replace(functionToOverride, replace);
                    scriptfileContent = scriptfileContent.replaceAll("4E3", "0");
                    functionCode = scriptfileContent;

                } else if (src.contains("_edge.js")) {
                    //logger.debug("src.contains(\"_edge.js\")");
                    String scriptfileContent = loadFile(scriptUrl);
                    scriptfileContent = scriptfileContent.replace("var im=", "var im = '" + rootUrl + "'+");
                    functionCode = functionName
                            + " = function(){"
                            + "console.log('function::" + functionName + "');\n"
                            + scriptfileContent
                            + "};";
                } else {
                    functionCode = functionName
                            + " = function(){"
                            + loadFile(scriptUrl)
                            + "};";
                }
                String newScript = "\n<script type=\"text/javascript\">\n"
                        + functionCode
                        + "\n</script>\n";
                html = html.substring(0, shiftPosition + matcher.start()) + newScript + html.substring(shiftPosition + matcher.end());
                shiftPosition += newScript.length() - matcher.end() + matcher.start();
            }
        }
        html = html.replaceAll("&lt;", "<");
        return html;
    }



    private static String getScr(String found) {
        String src = "src=(\"|')(.*\\.js)(\"|')";
        Matcher m = Pattern.compile(src).matcher(found);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

}