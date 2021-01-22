/*TokenTest.c*/
//i_a/*comment*/=4;		// これは「i_a=4;」。= が消えてなくなっていないか？

/*SemanticTest.c*/
// 代入文に関する構文解析＆意味解析テスト

// (1) 整数型の扱い
//i_a=0;	// 正当（生成コードが正しいかどうかも確認）
//*i_a=1;	// 不当
//i_a[3]=1;	// 不当
//i_a=&1;	// 不当
//i_a=0		// 構文解析エラー（セミコロンなし）
//i_a 0;	// 構文解析エラー（＝なし）

// (2) ポインタ型の扱い
//ip_a=1;	// 不当
//ip_a=&1;	// 正当（生成コードが正しいかどうかも確認）
//*ip_a=1;	// 正当（生成コードが正しいかどうかも確認）
//*10=1;	// 構文解析エラー

//ip_a=&1;*ip_a=1;	// 正当（複数文正当なら、両方とも解析とコード生成できることを確認せよ）

// (3) 配列型の扱い
//ia_a=1;	// 不当
//ia_a=ia_a;	// 不当（正当だとすると、配列全体をごっそりコピーするのですか？）
//ia_a[3]=1;	// 正当（生成コードが正しいかどうかも確認）
//ia_a[3=1;	// 構文解析エラー（]が閉じてない）
//ia_a 3]=1;	// 構文解析エラー（[が開いてない…「＝がない」というエラーになるはず）

// (4) 定数には代入できないことの確認
c_a=1;	// 不当

