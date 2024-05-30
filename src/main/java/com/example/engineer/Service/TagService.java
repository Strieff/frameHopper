package com.example.engineer.Service;

import com.example.engineer.Model.Frame;
import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import com.example.engineer.Repository.FrameRepository;
import com.example.engineer.Repository.TagRepository;
import com.example.engineer.Repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final FrameRepository frameRepository;

    public List<Tag> getAllTags(){
        return tagRepository.findAll();
    }

    public Tag getByName(String name){
        return tagRepository.findByName(name).stream().findFirst().orElse(null);
    }

    public Tag createTag(String name,Double value,String description){
        return tagRepository.save(Tag.builder()
                        .name(name)
                        .value(value)
                        .description(description)
                        .build());
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
        for(Frame f : frameRepository.getAllFramesWithTag(tag)){
            f.getTags().remove(IntStream.range(0,f.getTags().size())
                    .filter(i -> f.getTags().get(i).getId().equals(tag.getId()))
                    .findFirst()
                    .orElse(-1));

            frameRepository.save(f);
        }

        //clear the list of
        tag.setFrames(null);

        //delete tag
        tagRepository.delete(tag);
    }

    public Map<Video,Long> getAmountOfUniqueTagsOnVideos(List<Video> videos){
        Map<Video,Long> map = new HashMap<>();
        List<Object[]> data = tagRepository.countUniqueTagsByVideo(videos);

        for(Object[] o : data)
            map.put((Video)o[0], (Long)o[1]);

        return map;
    }

    public List<Object[]> countTagsOnFramesOfVideo(Video video){
        return tagRepository.countTagOccurrencesInVideoFrames(video);
    }

    public List<Object[]> countTagsOnFramesOfVideo(List<Integer> videoIds){
        return tagRepository.countTagOccurrencesInVideoFrames(videoIds);
    }
}
