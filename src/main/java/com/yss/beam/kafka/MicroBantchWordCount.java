package com.yss.beam.kafka;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * bin/kafka-console-producer.sh --broker-list 192.168.165.20:9092 --topic DevelopTest
 * bin/kafka-topics.sh --create --replication-factor 1 --partitions 1 --topic DevelopTest --zookeeper localhost:2181/kafka
 */
/**
 * @author MingZhang Wang
 */
public class MicroBantchWordCount {
    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        KafkaIO.Read<String, String> kafkaRead = KafkaIO.<String, String>read()
                .withBootstrapServers("192.168.165.21:9092")
                .withTopic("DevelopTest")
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class);

        PCollection<KV<String, String>> pkv = pipeline.apply(kafkaRead.withoutMetadata());

        pkv.apply(MapElements.via(new SimpleFunction<KV<String, String>, Integer>() {
                    @Override
                    public Integer apply(KV<String, String> input) {
                        System.out.println("old data : " + input);
                        return Integer.valueOf(input.getValue());
                    }
                }))
//                .apply(Window.into(FixedWindows.of(Duration.standardSeconds(5))))
//                .apply(Sum.integersGlobally())
                .apply(ParDo.of(new Println()));

        pipeline.run();

    }
    static class Println extends DoFn<Integer, Integer> {
        /**
         * processElement，过程元素处理方法，类似于spark、mr中的map操作
         * 必须加上@ProcessElement竹节，并实现processElement方法
         */
        @ProcessElement
        public void processElement(ProcessContext context){
            // 从管道中取出的每个元素
            Integer element = context.element();
            System.out.println("new data : " + element);
            if (element!=null){
                context.output(element);
            }
        }
    }

}
