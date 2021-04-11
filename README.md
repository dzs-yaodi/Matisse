# Matisse
知乎图片视频选择器，降androidx版本，新增视频选择器预览功能

原地址：![https://github.com/zhihu/Matisse](https://github.com/zhihu/Matisse)

因为老项目的原因，把androidx给降级了

保留了原先的使用方式，当 MimeType.ofVideo() 的时候，跳转新的视频选择页面。目前视频只做了单选功能，等以后有时间了再去完善。

在调用页面onActivityResult 方法中

String path = data.getStringExtra(CustomVideoActivity.EXTRA_RESULT_VIDEO_URI);

即可获得选中的视频路径

使用方法：

maven { url 'https://jitpack.io' }

implementation com.github.dzs-yaodi:Matisse:1.0.8

1.0.8 适配小米6无法通过cursor读取视频时长的问题
