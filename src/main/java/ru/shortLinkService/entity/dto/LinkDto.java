package ru.shortLinkService.entity.dto;

import ru.shortLinkService.entity.LinkRelation;

import java.util.Set;

public class LinkDto {

    private final Set<LinkRelation> linkRelations;
    private final int numberOfReferences;
    private final Set<LinkRelation> linkDeleted;
    private final int numberLinksDeleted;

    public LinkDto(Set<LinkRelation> linkRelations, int numberOfReferences,Set<LinkRelation> linkDeleted, int numberLinksDeleted ) {
        this.linkRelations = linkRelations;
        this.numberOfReferences = numberOfReferences;
        this.linkDeleted = linkDeleted;
        this.numberLinksDeleted = numberLinksDeleted;
    }

    public int getNumberOfReferences() {
        return numberOfReferences;
    }

    public Set<LinkRelation> getLinkRelations() {
        return linkRelations;
    }

    public int getNumberLinksDeleted(){return numberLinksDeleted;}

    public Set<LinkRelation> getLinkDeleted() {return linkDeleted;}
}
