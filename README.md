# MeTime

### 介绍：
多媒体app，含音乐播放服务、视频播放、相册、记事本。设计了许多ui动画提升视觉效果，同时进行了一定的性能优化

自己手搓的练手用项目，顺便作为个人使用app，想到就会写一下，有新想法就重构

包含歌单、相册、记事本三个模块，封装后台音乐播放服务，通知栏小播放器，封装了视频播放器，通过spannable string实现富文本编辑器(一开始是js)
arouter作为组件化工具，新重构的模块为MVVM，使用room作为数据库、datastore作为用户数据本地存储，glid作为图片加载

### 自定义view:
实时高斯模糊  
动态渐变进度条，通过该进度条实现出拖动条，颜色选择器  
动态渐变view  
支持手势的区块加载ImageView  
富文本View  
视频播放器  
音乐播放器(大中小)  
...  

### 预览：  
![图片](Screenshots/Screenshot02_MeTime.jpg)
![图片](Screenshots/Screenshot13_MeTime.jpg)
![图片](Screenshots/Screenshot24_MeTime.jpg)
![图片](Screenshots/Screenshot33_MeTime.jpg)
![图片](Screenshots/Screenshot37_MeTime.jpg)
![图片](Screenshots/Screenshot42_MeTime.jpg)
![图片](Screenshots/Screenshot46_MeTime.jpg)
