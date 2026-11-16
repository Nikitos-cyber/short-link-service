package ru.shortLinkService.dao;

import org.springframework.stereotype.Repository;
import ru.shortLinkService.entity.LinkRelation;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class DeletedLinkDao {
    private ConcurrentMap<String, Set<LinkRelation>> linksDeleted;

    public DeletedLinkDao() {
        linksDeleted = new ConcurrentHashMap<>();
    }

    public Set<LinkRelation> getAllLinkUser(String UUID) {
        if (!linksDeleted.containsKey(UUID))
            return new HashSet<>();
        return new HashSet<>(linksDeleted.get(UUID));
    }

    public void saveLink(String UUID, LinkRelation link) {

        if (linksDeleted.containsKey(UUID)) {
            if (linksDeleted.get(UUID).contains(link))
                return;
            linksDeleted.get(UUID).add(link);
            return;
        }

        linksDeleted.put(UUID, new HashSet<>());
        linksDeleted.get(UUID).add(link);

    }


    public void removeAllUser(String UUID){
        if (linksDeleted.containsKey(UUID)) {
            linksDeleted.get(UUID).clear();
        }
    }






}
