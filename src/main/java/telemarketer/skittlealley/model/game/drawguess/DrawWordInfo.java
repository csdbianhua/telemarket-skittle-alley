package telemarketer.skittlealley.model.game.drawguess;

/**
 * Author: Hanson
 * Time: 17-2-11
 * Email: imyijie@outlook.com
 */
public class DrawWordInfo {

    private Integer id;
    private String word;
    private Integer wordCount;
    private String wordType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }
}
