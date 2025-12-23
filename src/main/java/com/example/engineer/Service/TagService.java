package com.example.engineer.Service;

import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Repository.TagRepository;
import jakarta.transaction.Transactional;
import javafx.util.Pair;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags(){
        return tagRepository.findAll();
    }

    public List<Tag> getAllEnriched(){
        return tagRepository.findAllEnriched();
    }

    public List<Object[]> getAllEnriched(Video video) {
        return tagRepository.countTagOccurrencesInVideoFrames(video);
    }

    public Tag createTag(String name,Double value,String description){
        return tagRepository.save(Tag.builder()
                .name(name)
                .value(value)
                .description(description)
                .build());
    }

    public Tag getTag(int id){
        return tagRepository.findById((double) id).orElse(null);
    }

    public Tag getTag(String name) {
        return tagRepository.findByNameEnriched(name).orElse(null);
    }

    public Tag editTag(Integer id,String name,Double value,String description){
        return tagRepository.save(Tag.builder()
                .id(id)
                .name(name)
                .value(value)
                .description(description)
                .build());
    }

    @Transactional
    public void setHiddenStatus(Integer id,boolean hide){
        if(hide)
            tagRepository.hideTag(id);
        else
            tagRepository.unHideTag(id);
    }

    @Transactional
    public void deleteTag(Tag tag) {
        //remove tag from all the associated frames
        deleteTagRelations(List.of(tag));

        //clear the list of
        tag.setFrames(null);

        //delete tag
        tagRepository.delete(tag);
    }

    @Transactional
    public void deleteTag(List<Tag> tags){
        deleteTagRelations(tags);

        tagRepository.batchTagDelete(tags.stream().map(Tag::getId).toList());
    }

    @Transactional
    public void deleteTagRelations(List<Tag> tags){
        //delete entities in many-to-many table
        tagRepository.totalDeleteTags(tags.stream().map(Tag::getId).toList());
    }

    public List<Object[]> countTagsOnFramesOfVideo(List<Integer> videoIds){
        return tagRepository.countTagOccurrencesInVideoFrames(videoIds);
    }

    public Map<Video,Long> getAmountOfUniqueTagsOnVideos(List<Video> videos){
        Map<Video,Long> map = new HashMap<>();
        List<Object[]> data = tagRepository.countUniqueTagsByVideo(videos);

        for(Object[] o : data)
            map.put((Video)o[0], (Long)o[1]);

        return map;
    }

    public Tag getById(int id) {
        return tagRepository.findByIdEnriched(id).orElse(null);
    }

    public Map<Tag, Pair<Double,Integer>> getAllTagData(List<Video> videos) {
        var tags = videos.stream()
                .map(Video::getFrames)
                .flatMap(List::stream)
                .map(Frame::getTags)
                .flatMap(List::stream)
                .toList();

        var pointSumMap = new HashMap<Tag,Double>();
        tags.forEach(t -> pointSumMap.merge(t,t.getValue(),Double::sum));

        var occurrenceSumMap = new HashMap<Tag,Integer>();
        videos.stream()
                .map(Video::getFrames)
                .flatMap(List::stream)
                .forEach(f -> f.getTags()
                        .forEach(t -> occurrenceSumMap.merge(t,1,Integer::sum))
                );

        var dataMap = new HashMap<Tag, Pair<Double,Integer>>();
        for(var t : pointSumMap.keySet())
            dataMap.put(t,new Pair<>(pointSumMap.get(t), occurrenceSumMap.get(t)));

        return dataMap;
    }
}
