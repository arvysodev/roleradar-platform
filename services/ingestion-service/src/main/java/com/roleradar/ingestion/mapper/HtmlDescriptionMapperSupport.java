package com.roleradar.ingestion.mapper;

import org.jsoup.Jsoup;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class HtmlDescriptionMapperSupport {

    @Named("htmlToPlainText")
    public String htmlToPlainText(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }

        String text = Jsoup.parse(html).text();
        return text.isBlank() ? null : text;
    }

    @Named("plainTextToHtml")
    public String plainTextToHtml(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        return "<p>" + escaped + "</p>";
    }
}
