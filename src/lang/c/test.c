/*TokenTest.c*/
//if{else}endif;while(input)output;

/*ParseTest.s*/

// 構文解析＆意味解析テスト
//	if, while, input, outputの文法が自分のと異なる場合は、
//	自分の文法に合うように直して使うこと。

if (true) {	// true を 3 に変えてエラーになることを確認
   i_a=1;
} else {	// { を消したらエラーになることを確認
   i_a=2;
}

if (false) {	// ( と ) をそれぞれ単独で消したときエラーになることを確認
   i_a=3;
}

if (i_a == 3) {
   i_a=0;
} else if {	// キーワードelseifを導入した人は、そのように直すこと
   i_a=1;
} else {
   i_a=2;
}
//else {	// コメントを外して、elseが複数あるのは文法エラーになることを確認
//   i_a=3;
//}

while (true) {		// whileのつづりを間違えてみてエラーになることを確認
   input i_a;		// ; を消したときにエラーになることを確認
//   input ip_a;	// ポインタ変数に入力するのもOK
//   input 3;		// コメントを外したら文法エラーになることを確認
//   input c_b;		// コメントを外したら意味解析エラーになることを確認（定数には読み込めない）
//   input i_a+2;	// どの場所へ入力値を格納するのですか？　エラーでなければおかしい
//   output &i_a;		// この文にエラーはない
}			// } を消したらエラーになることを確認


/*CodeTest.c*/

// コード生成テスト
//	if, while, input, outputの文法が自分のと異なる場合は、
//	自分の文法に合うように直して使うこと。
//
//	ラベルを置く位置、分岐命令の後ろのジャンプ先ラベル名が合っているか確認する

if (false) {
   i_a=3;
}

if (true) {
   i_a=1;
} else {
   i_a=2;
}

while (true) {
   input i_a;
   while (false) {
      output i_a;
   }
   i_a=4;
}

if (true) if (false) if (true) if (false) i_a = 0; else i_b = 1;