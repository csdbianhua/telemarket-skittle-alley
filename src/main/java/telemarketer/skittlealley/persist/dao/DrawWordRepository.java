package telemarketer.skittlealley.persist.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import telemarketer.skittlealley.model.game.drawguess.DrawWord;

/**
 * Author: Hanson
 * Time: 17-2-11
 * Email: imyijie@outlook.com
 */
public interface DrawWordRepository extends JpaRepository<DrawWord, Integer> {

}
