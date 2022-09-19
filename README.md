----

- author: xuming Ye
- date: 09/06/2021 3.pm

----



**主题：采用变粒度分块以及元数据大小，提高去重速度和精度**

**目录结构**

-  实验数据文件夹，即为了测试代码准确性创建的文件夹
  - images
  - **META 这个文件夹是存放了Jar包的信息，但是也不用看，因此也放在这里**
  - out里面包含了之前所生成的jar包文件，不用管
  - res
  - result_finesse
  - block_all_hash
  - result_our_method 这四个都是实验数据保存的文件夹
  - log4j2.xml是日志框架的配置文件
  - 其他的txt文件是用来辅助测试的
- **关键内容**
  - src /test 文件夹下面都是一些单元测试内容，测试一些函数的准确性等等
  - utils里面包含了很多基础类，具体如下：
    - Chunk 数据块信息
    - Metadata 保存了元数据信息
    - MyFileUtil 定义了很多静态函数，对文件进行操作
    - Properties 配置了文件的路径
    - RabinHashFunction 为rabin fingerprint的Java实现
    - VCdiff即Delta encoding

**数据所依赖的部分包存在lib文件夹中，其余通过Maven获得。具体内容并没有详细说明，整体代码都是根据论文的逻辑结构来编写的，理解上应该不存在较大的问题。但是由于本人的能力有限，代码肯定多多少少存在某些问题，代码风格不够规范，希望之后的同学能够理解。**

--------

**代码结构**

- java
  - duduplicator
    - Main 主函数
    - ResemblanceDetection 具体实现类，可根据传入的参数不同执行不同的原始去重算法，如odess, finesse, ntransform
  - naswithcard
    - Main 主函数
    - OptFinesse 具体实现类

如何使用？

将项目打包，然后java -jar **.jar  --parameters... 即可