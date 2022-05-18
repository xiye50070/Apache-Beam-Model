# Apache Beam

## 一、概述

Apache Beam是一套编程模型和接口范式，是Google在2016年2月份贡献给Apache基金会的Apache孵化项目，其思想源自于[Google DataFlow Model![ ](https://csdnimg.cn/release/blog_editor_html/release2.1.0/ckeditor/plugins/CsdnLink/icons/icon-default.png?t=M3K6)https://link.zhihu.com/?target=https%3A//research.google/pubs/pub43864/](https://link.zhihu.com/?target=https%3A//research.google/pubs/pub43864/)这篇论文。论文中作者论证了流处理系统其实是批处理系统的超集，即batch只是固定process/event time window下，利用atWaterMark trigger的数据处理模型。这套理论体系已经成为当前构建分布式流式处理系统的思想指导，而Beam正是这个思维的具象化实现。论文认为一个好的工具需要能够动态根据用户的使用场景、配置进行适应，具体的技术细节由工具本身消化，论文还认为需要摆脱目前一个主流的观点，认为执行引擎负责描述系统的语义。合理设计和构建的批，微批次，流处理系统能够保证同样程度的准确性。而这三种系统在处理无边界数据流时都非常常见。如果我们抽象出一个具有足够普遍性，灵活性的模型，那么执行引擎的选择就只是延迟程度和处理成本之间的选择。

Beam实现了计算逻辑的统一表达，本身并不**处理**数据，而是用来**描述**一个数据处理任务应该如何进行的，真正运行处理的是Runner。Beam目前支持Spark2.4+以及Flink1.11+



## 二、Apache Beam 能做什么

Aache Beam 将批处理和流式数据处理融合在一起，他可以做到一套符合Beam语义的代码，在多种执行引擎上运行，而且目前支持Java、Python、Go和 Scala语言，可以做到用户每个人都可以使用自己熟悉的语言来进行开发

Apache Beam 支持的执行引擎有：

- Apache Flink
- Apache Spark
- Apache Apex
- Apache Gearpump (incubating)
- Apache Samza
- Google Cloud Dataflow
- Hazelcast Jet



## 三、Apache Beam 核心编程模型

### Apache Beam 的核心概念有：

| 编程模型       | 说明                                                         |
| :------------- | ------------------------------------------------------------ |
| PCollection    | 数据集，代表了将要被处理的数据集合，可以是有限的数据集，也可以是无限的数据流。 |
| PTransform     | 计算过程，代表了将输入数据集处理成输出数据集中间的计算过程。 |
| Pipeline       | 管道，代表了处理数据的执行任务，可视作一个有向无环图（DAG），PCollections是节点，Transforms是边。 |
| PipelineRunner | 执行器，指定了Pipeline将要在哪里，怎样的运行。               |
| ParDo          | 通用的并行处理的PTranform， 相当于Map/Shuffle/Reduce-style 中的Map，可用于过滤 、类型转换 、抽取部分数据 、 对数据中的每一个元素做计算等 |
| Watermark      | 标记了多久时间后的延迟数据直接抛弃                           |
| Triggers       | 用来决定什么时候发送每个window的聚合结果                     |

在使用Apache Beam开发时，需要考虑几个关键点

1. 数据输入
2. 输入的数据类型
3. 数据要做什么样的转换（计算）
4. 数据输出



### 单pipeline的线性基础模型

单pipeline的Apache Beam的编程模型为：

![线性流水线以一个输入集合开始，依次应用三个变换，并以一个输出集合结束。](https://beam.apache.org/images/design-your-pipeline-linear.svg)



### 