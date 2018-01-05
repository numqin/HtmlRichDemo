#WebView 显示HTML富文本

首先说明下富文本是什么：富文本的定义是一种跨平台的文本处理方式。

浏览大多数的论坛博客，发现 android 显示富文本的途径主要有两种：

1. 将 Html 文本转成 SpannableString ，通过 TextView 显示。
2. 利用 Webview 显示 Html

##Html 文本转成 SpannableString 

> 这里简单的提一下 Html 文本转成 SpannableString ，这种方式主要是通过识别 html 的标签，然后将内容转成 SpannableString 。
>
> android sdk 中有现成的方法可以使用 Html.fromhtml 不过它并不能识别所有的 Html 标签。
>
> 如果自己处理 Html 标签的太费时间，而且本人对 Html 并不是太熟，所以选择第二种，使用Webview 显示

## 使用 Webview 显示 Html 文本

> 如果你的 html 文本中使用到了网络上的图片请先把网络请求权限加上

- 核心方法

  ```Java
  webView.loadData(htmlStr, "text/html", "UTF-8");
  webView.loadDataWithBaseURL(null, htmlStr, "text/html", "UTF-8", null);
  ```

  上面的 htmlStr 即你的 Html 文本字符串

  实际使用中发现：webView.loadData() 这个方法中文显示存在乱码，使用webView.loadDataWithBaseURL() 时一切正常，暂时还不清楚里面的原因。所以这里主要介绍下 webView.loadDataWithBaseURL（）方法

  ```Java
  public void loadDataWithBaseURL(
    String baseUrl, String data, String mimeType, String encoding, String historyUrl)
  ```

  - baseUrl —— 在 html 文本中一些的图片的 src 地址可能是相对地址。像这样 

    ```
    https://upload.jianshu.io/users/upload_avatars/5382223/a.jpg
    如果 data 中图片的地址是 /users/upload_avatars/5382223/a.jpg
    那么你需要在 baseUrl 赋值 https://upload.jianshu.io
    ```

  - data —— html 文本

  - mimeType —— 文本类型

  - encoding —— 编码格式

  - historyUrl —— 不知道

## 上手使用

- 简单的使用

  ```java
  WebView webView = findViewById(R.id.web_view);
  //设置 webView
  webView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);//取消滚动条
  webView.getSettings().setSupportZoom(false);//不支持缩放功能
  //加载 html 文本
  webView.loadDataWithBaseURL(null, htmlStr, "text/html", "UTF-8", null);
  ```

- 图片的处理

  当然这简单的使用并不能满足我们的需求，当图片尺寸太大的时候还能横滑，这怎么行，

  ```groovy
  //依赖的库
  compile 'org.jsoup:jsoup:1.11.2'
  ```

  ```Java
  //这里我们使用 jsoup 修改 img 的属性:
  final Document doc = Jsoup.parse(htmltext);

  final Elements imgs = doc.getElementsByTag("img");
  for (int i = 0; i < imgs.size(); i++) {
     	//宽度填充手机，高度自适应
  	imgs.get(finalI).attr("style", "width: 100%; height: auto;");
  }
  ```

  ```Java
  //这里我们使用 jsoup 修改 embed 的属性:
  Elements embeds = doc.getElementsByTag("embed");
  for (Element element : embeds) {
      //宽度填充手机，高度自适应
      element.attr("width", "100%").attr("height", "auto");
  }

  //webview 无法正确识别 embed 为视频，所以这里把这个标签改成 video 手机就可以识别了
  doc.select("embed").tagName("video");
  ```

- 现在所有的图片都是宽度跟手机一样宽，高度自适应，像一些比较小的图如果还跟屏幕一样宽，这画质不能忍啊，如果不满意我们则需要再次处理。

  分两种情况：

  1. 标签中带有图片的宽高属性
  2. 跟我一样只有一个 src 

  两个的处理是一样的，都需要知道图片的宽高，通过对比图片的宽度和手机的宽度

  ```java
  if(图片的宽度>手机的宽度){
  	//宽度填充手机，高度自适应
    	imgs.get(finalI).attr("style", "width: 100%; height: auto;");
  }else {
    	//不需要任何改动
  }
  ```

  第一种情况下我们可以通过 jsoup 来获取定义的 width height 

  第二种情况下由于只有 src ，我们需要获取网络图片的宽高，我这里是直接通过 Glide 来获取它的宽高，这里的宽高是需要请求网络获取的，所以我们可以在处理到最后一张图片的时候通知，webview 去加载 Html 文本，而不是直接就加载。

  ```groovy
  //依赖的库
  compile 'com.github.bumptech.glide:glide:3.8.0'
  ```

  ```Java
  Glide.with(this)
     	.load(src)
     	.asBitmap()
     	.skipMemoryCache(true)
     	.diskCacheStrategy(DiskCacheStrategy.NONE)
     	.into(new SimpleTarget<Bitmap>() {
     		@Override
     		public void onResourceReady(
            Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
          	int width = bitmap.getWidth();
              int height = bitmap.getHeight();
                          }
      });
  ```

   

- 使用 jsoup 处理完后，就不是加载原来的 htmlStr 而是 Jsoup 的 Document

  ```Java
  webView.loadDataWithBaseURL(null, doc.toString(), "text/html", "UTF-8", null);
  ```