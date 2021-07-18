# Aging
老化クラフト

## 動作環境
- Minecraft 1.16.5

## コマンド一覧
- aging
    - start
        プラグイン有効化
    - stop
        プラグイン無効化
    - restart
        プラグイン再起動
    - conf
        - period <trik>
          1年経過にかかるtrik数 [default: 100]
        - init_age <age>
          リスポーン時の年齢 [default: 0]
        - rejuvenate_age <age>
          若返りアイテムを食べて若返る年齢 [default: 10]
    - set <baby|kids|young|adult|elderly> <playerName>
      指定したユーザーの世代を固定します。世代固定したユーザーは年数が経過しても歳を取りません。
    - unset <playerName>
      ユーザーの世代固定を解除します。
    