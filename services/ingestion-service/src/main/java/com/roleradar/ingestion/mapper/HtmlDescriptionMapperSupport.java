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
}
