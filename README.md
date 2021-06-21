# Aging
老化クラフト

## 動作環境
- Minecraft 1.16.5
- PaperMC 1.16.5

## コマンド一覧
- aging
    - start
        プラグイン有効化
    - stop
        プラグイン無効化
    - conf
        - period < trik >
          1年経過するのに必要なtrik数 [default: 20]
        - < baby | kids | young | adult | elderly >
            - walkspeed < count > 歩行速度(-1.0~1.0の範囲で指定する)
            - maxhp < count > HPの最大値を指定する
            - foodlevel < count > 空腹値の最大値を指定する
    - set < username > <baby | kids | young | adult | elderly>
      指定したユーザーの世代を固定します。世代固定したユーザーは年数が経過しても歳を取りません。
    - unset < username >
      ユーザーの世代固定を解除します。
    