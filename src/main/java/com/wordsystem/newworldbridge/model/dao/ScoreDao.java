// src/main/java/com/wordsystem/newworldbridge/model/dao/ScoreDao.java

package com.wordsystem.newworldbridge.model.dao;

import com.wordsystem.newworldbridge.dto.Score;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ScoreDao {

    // Insert Score
    @Insert("INSERT INTO score (id, user_score) VALUES (#{id}, #{userScore})")
    void setScore(Score score);

    // Update Score
    @Update("UPDATE score SET user_score = #{userScore} WHERE id = #{id}")
    void updateScore(@Param("id") int id, @Param("userScore") int userScore);

    // Delete Score
    @Delete("DELETE FROM score WHERE id = #{id}")
    void deleteScore(int id);

    // Get Score by ID with explicit mapping
    @Select("SELECT * FROM score WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userScore", column = "user_score")
    })
    Score getScore(int id);

    // Get All Scores with explicit mapping
    @Select("SELECT * FROM score")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userScore", column = "user_score")
    })
    List<Score> getAllScores();
}
