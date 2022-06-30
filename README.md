# 浴衣販売ECサイト　:sunflower: :kimono:
浴衣販売のECサイトです。
画像などを一部を除いています。

## 目次
- 使用ツール
- セットアップ
- 実装機能
## 使用ツール
フロント
- HTML
- CSS
- materialize(1.1.0)
- jQuery(1.11.3)

サーバー
- Java(openjdk 18.0.1.1)
- Springboot(2.7.0)
- Thymeleaf
- JavaScript

データベース
- PostgreSQL

## セットアップ
### Javaインストール
使用したのはopenjdk(18.0.1.1)です。  
インストールは[こちら](https://jdk.java.net/18/)から。

### Spring Tool Suiteインストール
使用したのはSpring Tools 4 for Eclipseです。  
最新バージョンは4.15.1です。  
インストールは[こちら](https://spring.io/tools)から。

### データベース
使用したのはPostgeSQLです。  
「student」という名前のデータベースを作成し、[こちら](https://docs.google.com/document/d/1qPmDEEQ5emsmlowiZsx1e-v_p-lIZqphPEnjqm9M5EI/edit)のSQLを実行してください。  

ER図は下記の通りです。  
<details>
  <summary>ER図</summary>
  準備中
</details>

## 実装機能
<details>
  <summary>サイトマップ</summary>
  
  ![サイトマップ](./img/sitemap.png)
</details>
<details>
  <summary>ユーザー登録・入力値チェック・ログイン・ログアウト機能</summary>
</details>
<details>
  <summary>商品検索時のオートコンプリート機能</summary>
</details>
<details>
  <summary>ショッピングカート機能</summary>
</details>
<details>
  <summary>注文完了メール送信機能</summary>
</details>
<details>
  <summary>外部APIを用いたクレジットカード決済（テスト環境）</summary>
  
  [stripe](https://stripe.com/jp)  のテスト環境を用いて、クレジットカードでの決済を導入しました。

</details>
<details>
  <summary></summary>
</details>
