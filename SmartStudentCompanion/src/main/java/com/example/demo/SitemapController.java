public class SitemapController {

}
package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SitemapController {

    @GetMapping(value = "/robots.txt", produces = "text/plain")
    @ResponseBody
    public String robots() {
        return """
User-agent: *
Allow: /

Sitemap: https://smart-student-companion-production.up.railway.app/sitemap.xml
""";
    }

    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    @ResponseBody
    public String sitemap() {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">

    <url>
        <loc>https://smart-student-companion-production.up.railway.app/</loc>
    </url>

    <url>
        <loc>https://smart-student-companion-production.up.railway.app/login</loc>
    </url>

</urlset>
""";
    }
}