package ru.shortLinkService.dao;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.shortLinkService.entity.LinkRelation;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Repository
public class LinkRelationDao{
    private  ConcurrentMap<String, Set<LinkRelation>> linkRelations;

    @Value("${path.database.key}")
    private String pathDatabaseKey;
    @Value("${file.name.database}")
    private String fileNameDatabase;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init(){
        Path filePath = getPath();
        if(!Files.exists(filePath)){
            return;
        }
        try(ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(filePath)))){
            Object map =  objectInputStream.readObject();

            if(!(map  instanceof ConcurrentHashMap<?,?>)){
                throw new IllegalStateException("Файл не содержит ConcurrentMap ");
            }

            linkRelations = (ConcurrentMap<String, Set<LinkRelation>>) map;
        }catch (IOException | ClassNotFoundException e){
            System.err.print("[ERROR] " + e.getMessage());
        }
    }

    @PreDestroy
    public void destroy(){
        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(getPath())))){
            objectOutputStream.writeObject(linkRelations);
            objectOutputStream.flush();
        }catch (IOException e){
            System.err.print("[ERROR] " + e.getMessage());
        }
    }

    public Path getPath(){
        String homeDir = System.getProperty(pathDatabaseKey);
        Path pathDataBaseFile = Paths.get(homeDir);
        return pathDataBaseFile.resolve(fileNameDatabase);
    }

    public LinkRelationDao()  {
        linkRelations = new ConcurrentHashMap<>();
    }

    public void saveLink(String UUID, LinkRelation link) {

        if (linkRelations.containsKey(UUID)) {
            if (linkRelations.get(UUID).contains(link))
                return;
            linkRelations.get(UUID).add(link);
            return;
        }

        linkRelations.put(UUID, new HashSet<>());
        linkRelations.get(UUID).add(link);
        destroy();
    }

    public Set<LinkRelation> getAllLinkUser(String UUID) {
        if (!linkRelations.containsKey(UUID))
            return new HashSet<>();
        return new HashSet<>(linkRelations.get(UUID));
    }

    public void updateLink(String UUID, LinkRelation linkRelation) {
       removeLink(UUID,linkRelation.getShortenedLink().toString());
        saveLink(UUID,linkRelation);
        destroy();
    }

    public void removeLink(String UUID, String url) {
        if (linkRelations.containsKey(UUID)) {
            linkRelations.get(UUID).removeIf(l -> l.getShortenedLink().equals(URI.create(url)));
        }
        destroy();
    }

    public Set<LinkRelation> getAllLink() {

        return linkRelations.values()
                .stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

    }

    public void removeLink(LinkRelation linkRelation) {

        Iterator<Map.Entry<String, Set<LinkRelation>>> mapIterator = linkRelations.entrySet().iterator();

        while (mapIterator.hasNext()) {

            Map.Entry<String, Set<LinkRelation>> entry = mapIterator.next();
            Iterator<LinkRelation> iterator = entry.getValue().iterator();

            while (iterator.hasNext()) {
                LinkRelation item = iterator.next();
                if (item.equals(linkRelation))
                    iterator.remove();
            }

            if (entry.getValue().isEmpty()) {
                mapIterator.remove();
            }
        }
        destroy();
    }


    public String findUser(LinkRelation linkRelation){
        Iterator<Map.Entry<String, Set<LinkRelation>>> mapIterator = linkRelations.entrySet().iterator();

        while (mapIterator.hasNext()) {

            Map.Entry<String, Set<LinkRelation>> entry = mapIterator.next();
            Iterator<LinkRelation> iterator = entry.getValue().iterator();

            while (iterator.hasNext()) {
                LinkRelation item = iterator.next();
                if (item.equals(linkRelation))
                    return entry.getKey();
            }

        }
        return null;
    }

    public LinkRelation findLink(LinkRelation linkRelation){
        Iterator<Map.Entry<String, Set<LinkRelation>>> mapIterator = linkRelations.entrySet().iterator();

        while (mapIterator.hasNext()) {

            Map.Entry<String, Set<LinkRelation>> entry = mapIterator.next();
            Iterator<LinkRelation> iterator = entry.getValue().iterator();

            while (iterator.hasNext()) {
                LinkRelation item = iterator.next();
                if (item.equals(linkRelation))
                    return item;
            }

        }
        return null;
    }


}