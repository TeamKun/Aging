package net.kunmc.lab.player;

import java.util.LinkedHashMap;

public class PlayerManager {
    private static PlayerManager playerManager = new PlayerManager();
    private LinkedHashMap<String, PlayerAttribute> playerMap;

    private PlayerManager() {

    }

    public static PlayerManager getInstance() {
        return playerManager;
    }

    public void login(String uuid) {
        if(null == playerMap) {
            return;
        }
        playerMap.put(uuid, new PlayerAttribute());
    }

    public void logout(String uuid) {
        if(null == playerMap) {
            return;
        }
        playerMap.remove(uuid);

    }

    public void reset(){
        playerMap = null;

        // FIXME: 再生成処理どうしようかな
    }

    // FIXME: ログ表示を考えるとTASKに移動したほうが良いかも
    public void agingPlayer() {
        if(playerMap == null) {
            return;
        }
        playerMap.forEach((uuid, playerAttribute)-> {
            playerAttribute.addAge();

            int nextGeneration = playerAttribute.findGeneration();
            // 寿命に到達した場合は即死
            if(nextGeneration < 0) {
                //TODO: 即死処理
            }

            // 世代を跨がない場合は年齢加算だけ
            if(playerAttribute.getGeneration() == playerAttribute.findGeneration()) {
                return;
            }

            // 加算後の年齢が次世代に更新される場合は世代更新を行う
            playerAttribute.addGeneration();
        });
    }
}
