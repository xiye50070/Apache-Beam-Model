package com.yss.beam.hdfs;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.hdfs.HadoopFileSystemOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;

public class BeamHdfsModel {
    public static void main(String[] args) {
        String[] args1 = new String[] {
                "--hdfsConfiguration=[{\"fs.defaultFS\":\"hdfs://henghe-042:9002\"}]" };

        HadoopFileSystemOptions options =
                PipelineOptionsFactory.fromArgs(args1).withValidation().as(
                        HadoopFileSystemOptions.class);

        Pipeline pipeline = Pipeline.create(options);
        pipeline.apply(TextIO.read().from("hdfs://henghe-042:9002/user/TestTxt.txt"))

                .apply(FlatMapElements.into(TypeDescriptors.strings()).via(line -> Arrays.asList(line.split(" "))))
//                .apply("write",TextIO.write().to("hdfs://henghe-042:9002/user/TestTxt2.txt"));
                .apply(Count.perElement())
                .apply(ParDo.of(new DoFn<KV<String, Long>, KV<String, Long>>() {
                    @DoFn.ProcessElement
                    public void processElement(ProcessContext context) {
                        // 从管道中取出的每个元素
                        KV<String, Long> element = context.element();
                        System.out.println(element);
                        if (element != null) {
                            context.output(element);
                        }
                    }
                }));

        pipeline.run();
    }

}
