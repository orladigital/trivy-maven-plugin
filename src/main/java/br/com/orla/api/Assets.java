package br.com.orla.api;

import com.alibaba.fastjson.annotation.JSONField;

public class Assets {

    private String name;

    @JSONField(alternateNames = "browser_download_url")
    private String browserDownloadUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrowserDownloadUrl() {
        return browserDownloadUrl;
    }

    public void setBrowserDownloadUrl(String browserDownloadUrl) {
        this.browserDownloadUrl = browserDownloadUrl;
    }
}
