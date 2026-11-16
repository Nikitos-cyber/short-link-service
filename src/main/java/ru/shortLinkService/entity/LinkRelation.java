package ru.shortLinkService.entity;



import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.Objects;

public class LinkRelation implements Serializable {

    public static final long serializableVersion = 1L;

    private final URL fullLink;
    private final URI shortenedLink;
    private int limit;
    private int countFollowTheLinks;
    private final LocalDateTime registrationTimeLink;
    private LocalDateTime timeToLeaveLink;
    private String formatTime;
    private int timeDiff;
    private boolean hasTimeToLeaveLink;
    private String suffixShortenLink;

    private LinkRelation(URL fullLink, URL shortenedLink, int limit) throws URISyntaxException {

        this.limit = limit;
        countFollowTheLinks = 0;
        this.fullLink = fullLink;
        this.shortenedLink = shortenedLink.toURI();
        registrationTimeLink = LocalDateTime.now();
        this.hasTimeToLeaveLink = true;
    }

    public LinkRelation(URL fullLink, URL shortenedLink, int limit, TemporalAmount timeToLeaveLink) throws URISyntaxException {

        this(fullLink, shortenedLink, limit);
        this.timeToLeaveLink = registrationTimeLink.plus(timeToLeaveLink);
    }

    public String getSuffixShortenLink(){
        String shortLink = shortenedLink.toString();
        int indexNext = shortLink.indexOf('/');
        int indexCut = 0;
        while(indexNext  != -1 ){
            indexCut = indexNext;
            indexNext = shortLink.indexOf('/', indexNext  + 1);
        }

        return shortLink.substring(indexCut);
    }

    public void setTimeToLeaveLink(LocalDateTime time) {
        timeToLeaveLink = time;
    }

    public URL getFullLink() {
        return fullLink;
    }

    public URI getShortenedLink() {
        return shortenedLink;
    }

    public LocalDateTime getTimeToLeaveLink() {
        if(!hasTimeToLeaveLink)
            return null;
        return timeToLeaveLink;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setHasTimeToLeaveLink(boolean hasTimeToLeaveLink){
        this.hasTimeToLeaveLink = hasTimeToLeaveLink;
    }

    public String getRegistrationTimeLink(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyy");
        return registrationTimeLink.format(formatter) + " года";
    }

    public void setTimeDiff(int timeDiff){
        this.timeDiff = timeDiff;
    }

    public int getTimeDiff(){
        return timeDiff;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        LinkRelation other = (LinkRelation) object;
        return Objects.equals(shortenedLink, other.shortenedLink);
    }

    @Override
    public int hashCode() {
        return shortenedLink.hashCode();
    }

    public void incrementCountFollowTheLink(){
        if(countFollowTheLinks + 1 < limit)
             countFollowTheLinks++;
    }

    public int getCountFollowTheLinks() {
        return countFollowTheLinks;
    }

    public void iterCountFollowTheLinks() {
        countFollowTheLinks++;
    }

    public String getFormatTime() {

        long diffTime = 0;

        if (timeToLeaveLink.isAfter(registrationTimeLink)) {
            diffTime = (timeToLeaveLink.atZone(ZoneId.of("Europe/Moscow")).toInstant().toEpochMilli() / 1000)
                    - (registrationTimeLink.atZone(ZoneId.of("Europe/Moscow")).toInstant().toEpochMilli() / 1000);
        } else {
            diffTime = (registrationTimeLink.atZone(ZoneId.of("Europe/Moscow")).toInstant().toEpochMilli() / 1000)
                    - (timeToLeaveLink.atZone(ZoneId.of("Europe/Moscow")).toInstant().toEpochMilli() / 1000);
        }

        if (diffTime < 3_600) {

            int minutes = (int) diffTime / 60;
            setTimeDiff(minutes);
            return minutes + " мин";

        } else if (diffTime < 3_600 * 24) {
            int hours = (int) diffTime / 3600;
            setTimeDiff(hours);
            return hours + " ч";
        }

        int days = (int)diffTime / 86_400;
        setTimeDiff(days);
        return  days + ( days >= 10 && days <= 20 ? " дней" : days % 10  < 1 || days % 10 > 4 ? " дней" : days % 10 == 1 ? " день" : " дня");


    }
}