package com.yss.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.*;

import java.io.IOException;
import java.util.Arrays;
/**
 * @author MingZhang Wang
 */
public class BantchWordCount {
    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<KV<String, Long>> pCollection = pipeline
//                .apply(TextIO.read().from("src/main/java/org/apache/beam/examples/yss/input/text.txt"))
                .apply(Create.of("hello world","hello flink", "hello flink spark"))
                .apply(FlatMapElements.into(TypeDescriptors.strings()).via(line -> Arrays.asList(line.split(" "))))
                .apply(Count.perElement());
        pCollection.apply(ParDo.of(new Println()));
        pipeline.run();
    }

    // DoFn<a,b> a是输入类型，b是输出类型
    static class Println extends DoFn<KV<String, Long>, String>{
        /**
         * processElement，过程元素处理方法，类似于spark、mr中的map操作
         * 必须加上@ProcessElement竹节，并实现processElement方法
         */
        @ProcessElement
        public void processElement(ProcessContext context){
            // 从管道中取出的每个元素
            KV<String, Long> element = context.element();
            System.out.println(element);
            if (element!=null){
                context.output(element.toString());
            }
        }
    }
}

