# WebApiAnalysis
项目介绍：
1.利用静态分析方法，绘制控制流程图和数据依赖图，通过对应用程序进行反向切片，恢复目标方法的参数。
2.恢复大量app应用在通信时使用的url信息，得到应用和第三方服务端的交互信息。
3.恢复与第三方应用交互的token的key值，检验是否可以利用这个key值在无授权的情况下访问第三方服务资源。

该项目基于LeakScope二次开发：https://github.com/OSUSecLab/LeakScope
