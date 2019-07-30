package com.example.ishii.rssreader

import android.content.Context
import android.content.AsyncTaskLoader
import org.w3c.dom.NodeList
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


//RSSの各記事を表すデータクラス
data class Article(val title: String, val link: String, val pubDate: Date)

//RSSを表現するデータクラス
data class Rss(val title: String, val pubDate: Date, val article: List<Article>)

fun parseRss(stream: InputStream) : Rss{

    //XMLをDOMオブジェクトに変換する
    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
    stream.close()

    //XPathを生成する
    val xPath = XPathFactory.newInstance().newXPath()

    //RSS2.0の日付書式である、RFC1123をdate型に変化するためのクラス
    val formatter = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US)

    //チャンネル内の<item>要素をすべて取り出す
    val items = xPath.evaluate("/rss/channel//item",doc,XPathConstants.NODESET) as NodeList

    //RSSフィールド内の記事の一覧
    val articles = arrayListOf<Article>()

    //<item>の要素ごとに繰り返す
    for (i in 0 until items.length){
        val item = items.item(i)

        //Articleオブジェクトにまとめる
        val article = Article(
            title = xPath.evaluate("./title/text()", item),
            link = xPath.evaluate("./link/text()",item),
            pubDate = formatter.parse(xPath.evaluate("./pubDate/text()",item)))

        articles.add(article)
    }

    //RSSオブジェクトにまとめて返す
    return Rss(title = xPath.evaluate("./rss/channel/title/text()", doc),
        pubDate = formatter.parse(xPath.evaluate("./rss/channel/title/text()",doc)),
        article = articles)
}

//RSSフィールドをダウンロードしてRssオブジェクトを返すローダー
class RssLoader(context: Context) : AsyncTaskLoader<Rss>(context){
    private var cache : Rss? = null

    override fun loadInBackground(): Rss? {
        //HTTPでRSSのXMLを取得する
        val response = httpGet("http://www.sbbit.jp/rss/HotTopics.rss")

        if (response != null){
            //取得に成功したら、バースして返す
            return  parseRss(response)
        }

        return null

    }

    override fun deliverResult(data: Rss?) {
        //破棄されていたら結果を返さない
        if (isReset || data == null) return

        //結果をキャッシュする
        cache = data
        super.deliverResult(data)
    }

    override fun onStartLoading() {
        //キャッシュがあるならキャッシュを返す
        if (cache != null){
            deliverResult(cache)
        }

        //コンテンツが変化している場合やキャッシュがない場合にはバックグラウンド処理を行う
        if (takeContentChanged() || cache == null){
            forceLoad()
        }
    }

    //ローダーが停止する前に呼ばれる処理
    override fun onStopLoading() {
        cancelLoad()
    }

    //ローダーが破棄される前に呼ばれる処理
    override fun onReset() {
        super.onReset()
        onStopLoading()
        cache = null
    }
}