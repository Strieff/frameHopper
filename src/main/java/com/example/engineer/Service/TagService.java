package com.example.engineer.Service;

import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Repository.FrameRepository;
import com.example.engineer.Repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final FrameRepository frameRepository;

    public List<Tag> getAllTags(){
        return tagRepository.findAll();
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

    @Transactional
    public void hideTags(List<Integer> tagIds, boolean hideAction){
        tagRepository.batchHideTag(tagIds,hideAction);
    }

    public Map<Video,Long> getAmountOfUniqueTagsOnVideos(List<Video> videos){
        Map<Video,Long> map = new HashMap<>();
        List<Object[]> data = tagRepository.countUniqueTagsByVideo(videos);

        for(Object[] o : data)
            map.put((Video)o[0], (Long)o[1]);

        return map;
    }

    public List<Object[]> countTagsOnFramesOfVideo(List<Integer> videoIds){
        return tagRepository.countTagOccurrencesInVideoFrames(videoIds);
    }
}
