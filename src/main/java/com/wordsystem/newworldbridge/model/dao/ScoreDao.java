package com.wordsystem.newworldbridge.model.dao;

import com.wordsystem.newworldbridge.dto.Score;
import org.apache.ibatis.annotations.*;

public interface ScoreDao {

    // Set Score
    @Insert("INSERT INTO score (id, user_score) VALUES (#{id}, #{userScore})")
    void setScore(Score score);

    // Update Score
    @Update("UPDATE score SET user_score = #{userScore} WHERE id = #{id}")
    void updateScore(@Param("id") int id, @Param("userScore") int userScore);

    // Delete Score
    @Delete("DELETE FROM score WHERE id = #{id}")
    void deleteScore(int id);

    // Get Score
    @Select("SELECT * FROM score WHERE id = #{id}")
    Score getScore(int id);
}
