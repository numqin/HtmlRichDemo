package com.qinlei.htmlrichdemo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static android.view.View.SCROLLBARS_OUTSIDE_OVERLAY;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private boolean isOnPause;
    private String htmlStr = "<p>\n" +
            "    abcd\n" +
            "</p>\n" +
            "<p>\n" +
            "    一二三\n" +
            "</p>\n" +
            "<p>\n" +
            "    <embed type=\"application/x-shockwave-flash\" class=\"edui-faked-video\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\" src=\"http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4\" width=\"420\" height=\"280\" style=\"float:none\" wmode=\"transparent\" play=\"true\" loop=\"false\" menu=\"false\" allowscriptaccess=\"never\" allowfullscreen=\"true\"/>\n" +
            "</p>\n" +
            "<p>\n" +
            "    <img src=\"http://img.baidu.com/hi/jx2/j_0002.gif\"/>\n" +
            "</p>\n" +
            "<p>\n" +
            "    <img src=\"http://ueditor.baidu.com/server/umeditor/upload/demo.jpg\"/>\n" +
            "</p>\n" +
            "<p>\n" +
            "    <img src=\"https://upload.jianshu.io/admin_banners/web_images/4034/0b55b91246092470da696aa477bda35e44008739.jpg?imageMogr2/auto-orient/strip|imageView2/1/w/1250/h/540\"/>\n" +
            "</p>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.web_view);
        //设置 webView
        webView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);//取消滚动条
        webView.getSettings().setSupportZoom(false);//不支持缩放功能

//        simpleUse(webView);

//        imageFillWidth(webView);

        defineImageWidth(webView);
    }

    /**
     * 简单 加载 html 文本
     *
     * @param webView
     */
    private void simpleUse(WebView webView) {
        webView.loadDataWithBaseURL(null, htmlStr, "text/html", "UTF-8", null);
    }

    /**
     * 处理图片视频填充手机宽度
     *
     * @param webView
     */
    private void imageFillWidth(WebView webView) {
        Document doc = Jsoup.parse(htmlStr);

        //修改视频标签
        Elements embeds = doc.getElementsByTag("embed");
        for (Element element : embeds) {
            //宽度填充手机，高度自适应
            element.attr("width", "100%").attr("height", "auto");
        }
        //webview 无法正确识别 embed 为视频，所以这里把这个标签改成 video 手机就可以识别了
        doc.select("embed").tagName("video");

        //控制图片的大小
        Elements imgs = doc.getElementsByTag("img");
        for (int i = 0; i < imgs.size(); i++) {
            //宽度填充手机，高度自适应
            imgs.get(i).attr("style", "width: 100%; height: auto;");
        }

        //加载使用 jsoup 处理过的 html 文本
        webView.loadDataWithBaseURL(null, doc.toString(), "text/html", "UTF-8", null);
    }

    /**
     * 更好的处理图片的宽度高度
     *
     * @param webView
     */
    private void defineImageWidth(final WebView webView) {
        final Document doc = Jsoup.parse(htmlStr);

        //修改视频标签
        Elements embeds = doc.getElementsByTag("embed");
        for (Element element : embeds) {
            //宽度填充手机，高度自适应
            element.attr("width", "100%").attr("height", "auto");
        }
        //webview 无法正确识别 embed 为视频，所以这里把这个标签改成 video 手机就可以识别了
        doc.select("embed").tagName("video");

        //控制图片的大小
        final Elements imgs = doc.getElementsByTag("img");
        for (int i = 0; i < imgs.size(); i++) {
            final int finalI = i;
            Glide.with(this)
                    .load(imgs.get(i).attributes().get("src"))
                    .asBitmap()//强制Glide返回一个Bitmap对象
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            int width = bitmap.getWidth();
                            if (width > getScreenWidth()) {
                                imgs.get(finalI).attr("style", "width: 100%; height: auto;");
                            }

                            if (finalI == imgs.size() - 1) {
                                //加载使用 jsoup 处理过的 html 文本
                                webView.loadDataWithBaseURL(null, doc.toString(), "text/html", "UTF-8", null);
                            }

                        }
                    });
        }
    }

    public int getScreenWidth() {
        WindowManager wm1 = this.getWindowManager();
        int width = wm1.getDefaultDisplay().getWidth();
        return width;
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (isOnPause) {
                if (webView != null) {
                    webView.getClass().getMethod("onResume").invoke(webView, (Object[]) null);
                }
                isOnPause = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (webView != null) {
                webView.getClass().getMethod("onPause").invoke(webView, (Object[]) null);
                isOnPause = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();

            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
