USIP
====

Unlabeled data Sampling for Imbalanced Problem  

2クラスラベル付きのImbalanceなデータセットから、その分布を考慮して未ラベル事例をサンプリングするプログラム。  
k-NNにより密な分布を維持しつつ、サンプリングされた事例との距離を評価して全域のサンプルを抽出する。  
  
使用方法  
コンパイル  
javac USIP.java  
実行  
java USIP \<unlabeled_data_file\> \<minority_class_data_file\>  
  
ファイルフォーマット  
[フォーマット1] 1次元の数値データ(1行に1データ)  
[フォーマット2] データID,数値データ のcsv形式。  
詳しくはサンプルデータを参照。  
  
アルゴリズムについてはまたまとめます。

