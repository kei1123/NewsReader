package com.example.ishii.rssreader

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Loader
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.app.LoaderManager

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Rss> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ローダーを呼び出す
        loaderManager.initLoader(1,null, this)
    }

    //ローダーが要求されたときに呼ばれる
    override fun onCreateLoader(id: Int, args: Bundle?) = RssLoader(this)

    //ローダーがリセットされたときに呼ばれる
    override fun onLoaderReset(loader: Loader<Rss>?) {
        //何もしない
    }

    //ローダーで行った処理が終了したときに呼ばれる
    override fun onLoadFinished(Loader: Loader<Rss>?, data: Rss?) {
        //処理結果がnullではない場合
        if (data != null){
            //RecyclerViewをレイアウトから探す
            val recyclerView = findViewById<RecyclerView>(R.id.articles)

            //RSSの記事一覧のアダプター
            val adapter = ArticlesAdapter(this, data.article){article ->
                //記事をタップした時の処理
            }

            recyclerView.adapter = adapter

            //グリッド表示するレイアウトマネジャー
            val layoutManager = GridLayoutManager(this, 1)

            recyclerView.layoutManager = layoutManager
        }
    }
}
