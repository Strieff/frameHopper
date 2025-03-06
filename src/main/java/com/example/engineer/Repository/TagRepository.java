package com.example.engineer.Repository;

import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag,Double> {
    @Modifying(clearAutomatically = true)
    @Query("update Tag t set t.deleted = true where t.id=:id")
    void hideTag(@Param("id") Integer id);

    @Modifying(clearAutomatically = true)
    @Query("update Tag t set t.deleted = false where t.id=:id")
    void unHideTag(@Param("id") Integer id);

    @Query("select t.name, count(distinct t), t.value, t.description, t.deleted, t.id from Tag t join t.frames f where f.video in :videos group by f.video")
    List<Object[]> countUniqueTagsByVideo(@Param("videos") List<Video> videos);

    @Query("select t.name, count(t), t.value, t.description, t.deleted, t.id from Tag t join t.frames f where f.video = :video group by t.name")
    List<Object[]> countTagOccurrencesInVideoFrames(@Param("video") Video video);

    @Query("select v, t.name, count(t) from Tag t join t.frames f join f.video v where v.id in :videoIds group by t.name,v.name")
    List<Object[]> countTagOccurrencesInVideoFrames(@Param("videoIds") List<Integer> videoIds);

    @Modifying(clearAutomatically = true)
    @Query(value = "delete from FRAME_TAG where TAG_ID in :tags", nativeQuery = true)
    void totalDeleteTags(@Param("tags") List<Integer> tags);

    @Modifying(clearAutomatically = true)
    @Query("delete Tag t where t.id in :tags")
    void batchTagDelete(@Param("tags") List<Integer> tags);

    @Modifying(clearAutomatically = true)
    @Query("update Tag t set t.deleted = :hideAction where t.id in :tags")
    void batchHideTag(@Param("tags") List<Integer> tags, @Param("hideAction") boolean hideAction);

    @EntityGraph(attributePaths = "frames")
    @Query("select t from Tag t")
    List<Tag> findAllEnriched();

    @EntityGraph(attributePaths = "frames")
    @Query("select t from Tag t where t.id=:id")
    Optional<Tag> findByIdEnriched(@Param("id") int id);

    @EntityGraph(attributePaths = "frames")
    @Query("select t from Tag t where t.name=:name")
    Optional<Tag> findByNameEnriched(@Param("name") String name);
}
