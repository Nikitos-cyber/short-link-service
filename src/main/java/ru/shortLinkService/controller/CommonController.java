package ru.shortLinkService.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ru.shortLinkService.entity.dto.LinkDto;
import ru.shortLinkService.service.LinkService;
import java.util.Map;

@Controller
public class CommonController {

    private final LinkService service;
    private final static String cookieName = "UUID_COOKIE";

    @Autowired
    public CommonController(LinkService service) {
        this.service = service;
    }

    @RequestMapping("/")
    public String redirectHomePage() {
        return "redirect:/home";
    }

    @RequestMapping("/home")
    public String getMainPage(@CookieValue(name = cookieName, required = false) String raw, HttpServletResponse response, Model model) {
        final String UUID = service.generateUUID(raw, response).toString();
        LinkDto dto = service.findAllLinksUser(UUID);
        model.addAttribute("numberLinksUser", dto.getNumberOfReferences());
        model.addAttribute("links", dto.getLinkRelations());
        model.addAttribute("numberLinksDeleted", dto.getNumberLinksDeleted());
        model.addAttribute("linksDeleted", dto.getLinkDeleted());
        return "main-page";
    }

    @RequestMapping(value = "/add-link",  method = RequestMethod.POST)
    public String addLink(@CookieValue(name = cookieName, required = false) String raw, @RequestParam String url){

        service.saveLink(service.getUUID(raw).toString(), url);
        return "redirect:/home";
    }

    @RequestMapping(value ="/delete-link", method = RequestMethod.POST )
    public String deleteLink(@CookieValue(name = cookieName, required = false) String raw, @RequestParam String link)  {
        service.deleteLink(raw, link);
        return "redirect:/home";
    }

    @RequestMapping(value = "/update-link", method = RequestMethod.POST)
    public String updateLink(@CookieValue(name = cookieName, required = false) String raw, @RequestParam Map<String,String> params){
        service.updateLink(raw,params);
        return "redirect:/home";
    }

    @RequestMapping("/**")
    public String fallback(@CookieValue(name = cookieName, required = false )String raw,  HttpServletRequest request) {
        String uriSuffix = request.getRequestURI();
         service.followLink(raw,uriSuffix);
        return "redirect:/home";
    }

}
