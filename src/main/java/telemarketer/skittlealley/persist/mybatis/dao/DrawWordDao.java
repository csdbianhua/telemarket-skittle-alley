package telemarketer.skittlealley.persist.mybatis.dao;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import telemarketer.skittlealley.persist.mybatis.domain.DrawWord;

/**
 * @author hason
 * @version 17-9-18
 */
public interface DrawWordDao {

    @Select("SELECT * FROM `draw_word`\n" +
            "WHERE id >= (SELECT floor(RAND() * (SELECT MAX(id) FROM `draw_word`)))  \n" +
            "LIMIT 1;")
    @ResultMap("telemarketer.skittlealley.persist.mybatis.dao.DrawWordMapper.BaseResultMap")
    DrawWord randomWord();
}

