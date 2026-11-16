package ru.shortLinkService.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.shortLinkService.dao.DeletedLinkDao;
import ru.shortLinkService.dao.LinkRelationDao;
import ru.shortLinkService.entity.LinkRelation;
import ru.shortLinkService.entity.dto.LinkDto;
import ru.shortLinkService.exceptions.CompressedException;
import ru.shortLinkService.referenceСonversion.CompressibleLink;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class LinkService {
    private final CompressibleLink compressedLink;
    private final LinkRelationDao dao;
    private final DeletedLinkDao daoDeletedLink;
    private URI generateUrl;
    @Value("${url.company}")
    private String urlCompany;
    private final static String cookieName = "UUID_COOKIE";

    @Autowired
    public LinkService(LinkRelationDao dao, DeletedLinkDao daoDeletedLink, CompressibleLink compressedLink) throws CompressedException {
        this.dao = dao;
        this.daoDeletedLink = daoDeletedLink;
        this.compressedLink = compressedLink;
    }

    public LinkDto findAllLinksUser(String UUID) {

        Set<LinkRelation> linkRelations = dao.getAllLinkUser(UUID);
        Set<LinkRelation> linkDeleted = daoDeletedLink.getAllLinkUser(UUID);
        int numberLinksUser = linkRelations.size();
        int numberLinksDeleted = linkDeleted.size();
        daoDeletedLink.removeAllUser(UUID);
        return new LinkDto(linkRelations, numberLinksUser, linkDeleted, numberLinksDeleted);
    }

    @Scheduled(fixedRate = 1000)
    public void deletingLinksAfterTime() {

        List<LinkRelation> expiredLinks = dao.getAllLink()
                .stream()
                .filter(e -> (e.getTimeToLeaveLink() != null && e.getTimeToLeaveLink().isBefore(LocalDateTime.now())))
                .toList();

        for (LinkRelation linkRelation : expiredLinks) {
            dao.removeLink(linkRelation);
            String userUuid = dao.findUser(linkRelation);
            if (userUuid != null)
                daoDeletedLink.saveLink(userUuid, linkRelation);
        }

    }

    public boolean emptyUUID(String raw) {
        return raw == null;
    }

    public UUID generateUUID(String raw, HttpServletResponse response) {
        UUID userUuid = getUUID(raw);
        if (emptyUUID(raw)) {
            userUuid = UUID.randomUUID();
        }
        Cookie cookie = new Cookie(cookieName, userUuid.toString());
        cookie.setMaxAge(60 * 60 * 24 * 400);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
        return userUuid;
    }

    public UUID getUUID(String raw) {
        if (emptyUUID(raw))
            return null;
        return UUID.fromString(raw);
    }

    public void saveLink(String UUID, String url) {

        if (url.isBlank())
            return;

        URI fullUri = URI.create(url);
        URI domain = URI.create(urlCompany);

        boolean hasUrl;

        do {
            generateUrl = compressedLink.getShortUrl(domain);
            hasUrl = dao.getAllLink().stream().anyMatch(e -> e.getShortenedLink().equals(generateUrl));
        } while (hasUrl);

        try {
            dao.saveLink(UUID, new LinkRelation(fullUri.toURL(), generateUrl.toURL(), 10, Duration.ofHours(24)));
        } catch (MalformedURLException | URISyntaxException e) {
            System.err.print("[ERROR] " + e.getMessage());
        }
    }

    public void deleteLink(String raw, String link) {
        if (link.isBlank())
            return;

        URI shortUri = URI.create(link);
        URI fullUri = URI.create(urlCompany);
        LinkRelation findLink;

        try {
            findLink = new LinkRelation(fullUri.toURL(), shortUri.toURL(), 10, Duration.ofHours(24));
            LinkRelation deletedLink = dao.findLink(findLink);
            if (deletedLink != null)
                daoDeletedLink.saveLink(getUUID(raw).toString(), deletedLink);
            dao.removeLink(getUUID(raw).toString(), link);
        } catch (MalformedURLException | URISyntaxException e) {
            System.err.print("[ERROR] " + e.getMessage());
        }

    }

    public void updateLink(String raw, Map<String, String> param) {

        int limit = param.containsKey("unlimited") ? -1
                : param.get("limit").length() > 10 ? Integer.MAX_VALUE
                : Integer.parseInt(param.get("limit")) < -1 ? 0 : Integer.parseInt(param.get("limit"));

        String duration = param.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith("ttl-"))
                .findFirst()
                .orElseThrow(NullPointerException::new)
                .getValue();

        String UUID = getUUID(raw).toString();

        LinkRelation oldLink = dao.getAllLinkUser(UUID)
                .stream()
                .filter(e -> e.getShortenedLink().equals(URI.create(param.get("link"))))
                .findFirst()
                .orElseThrow(NullPointerException::new);

        oldLink.setLimit(oldLink.getCountFollowTheLinks() + limit);

        if (duration.equals("none")) {
            oldLink.setHasTimeToLeaveLink(false);
        } else {

            TemporalAmount temporalAmount;

            temporalAmount = switch (duration) {
                case "1h" -> Duration.ofHours(1);
                case "24h" -> Duration.ofHours(24);
                case "1m" -> Period.ofMonths(1);
                default -> Duration.ofMillis(0);
            };

            oldLink.setHasTimeToLeaveLink(true);
            oldLink.setTimeToLeaveLink(LocalDateTime.now().plus(temporalAmount));
        }

        dao.updateLink(UUID, oldLink);

    }

    public void followLink(String raw, String uriSuffix) {
        URI path = URI.create(uriSuffix);
        URI domain = URI.create(urlCompany);
        URI shortenUri = domain.resolve(path);

        String UUID = getUUID(raw).toString();
        Set<LinkRelation> linkRelations = dao.getAllLinkUser(UUID);
        LinkRelation updateLinkCountFollow = linkRelations
                .stream()
                .filter(e -> e.getShortenedLink().equals(shortenUri))
                .findFirst()
                .orElse(null);

        if (updateLinkCountFollow == null)
            return;

        if (updateLinkCountFollow.getLimit() == -1) {
            try {
                Desktop.getDesktop().browse(updateLinkCountFollow.getFullLink().toURI());
            } catch (IOException | URISyntaxException e) {
                System.err.print("[ERROR] " + e.getMessage());
            }
            return;
        }


        if (updateLinkCountFollow.getCountFollowTheLinks() + 1 < updateLinkCountFollow.getLimit()) {

            updateLinkCountFollow.incrementCountFollowTheLink();
            dao.updateLink(UUID, updateLinkCountFollow);

        } else
            dao.removeLink(UUID, shortenUri.toString());
        try {
            Desktop.getDesktop().browse(updateLinkCountFollow.getFullLink().toURI());
        } catch (IOException | URISyntaxException e) {
            System.err.print("[ERROR] " + e.getMessage());
        }
    }

}
