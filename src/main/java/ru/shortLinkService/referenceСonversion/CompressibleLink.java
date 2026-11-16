package ru.shortLinkService.referenceСonversion;

import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;


public interface CompressibleLink {
    public URI getShortUrl(URI uri ) ;
}
