package com.example.engineer.Repository;

import com.example.engineer.Model.Tag;
import com.example.engineer.Model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag,Double> {
    @Query("select t from Tag t join t.frames f where f.frameNumber=:frameNumber and f.video=:video")
    List<Tag> findAllByFrameNumberAndVideo(@Param("frameNumber") Integer frameNumber, @Param("video") Video video);

    @Query("select t from Tag t where t.name=:name")
    Optional<Tag> findByName(@Param("name") String name);

    @Modifying(clearAutomatically = true)
    @Query("update Tag t set t.deleted = true where t.id=:id")
    void hideTag(@Param("id") Integer id);

    @Modifying(clearAutomatically = true)
    @Query("update Tag t set t.deleted = false where t.id=:id")
    void unHideTag(@Param("id") Integer id);
}
