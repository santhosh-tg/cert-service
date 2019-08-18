package org.incredible;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.URL;

public class UrlManager {
    private static URL urlPath;
    private static Logger logger = Logger.getLogger(UrlManager.class);

    public static String getSharableUrl(String url) {
        url = removeQueryParams(url);
        return StringUtils.isNotBlank(url)?fetchFileFromUrl(url):url;
    }

    private static String removeQueryParams(String url) {
        return StringUtils.isNotBlank(url)?url.split("\\?")[0]:url;
    }

    private static String fetchFileFromUrl(String url) {
        try {
            urlPath = new URL(url);
            return urlPath.getFile();
        } catch (Exception e) {
            logger.error("UrlManager:getUriFromUrl:some error occurred in fetch fileName from Url:".concat(url));
            return StringUtils.EMPTY;
        }
    }
}
