package com.yss.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
/**
 * @author MingZhang Wang
 */
public class BeamMutiOutput {
    public static void main(String[] args) {

        final TupleTag<String> endWithWorldTag = new TupleTag<String>();
        final TupleTag<String> endWithSparkTag = new TupleTag<String>();
        final TupleTag<String> endWithFlinkTag = new TupleTag<String>();

        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollectionTuple resultTuple = pipeline
                .apply(Create.of("hello world", "hello flink", "hello flink spark"))
                .apply(ParDo.of(new DoFn<String, String>() {
                    @ProcessElement
                    public void processElement(@Element String word, MultiOutputReceiver out) {
                        if (word.endsWith("world")) {
                            out.get(endWithWorldTag).output(word);
                        } else if (word.endsWith("spark")) {
                            out.get(endWithSparkTag).output(word);
                        } else {
                            out.get(endWithFlinkTag).output(word);
                        }
                    }
                }).withOutputTags(endWithFlinkTag, TupleTagList.of(endWithSparkTag).and(endWithWorldTag)));

        resultTuple.get(endWithFlinkTag).setCoder(StringUtf8Coder.of()).apply(ParDo.of(new DoFn<String, String>() {
            @ProcessElement
            public void processElement(@Element String element){
                System.out.println("flink Tag " + element);
            }
        }));

        resultTuple.get(endWithSparkTag).setCoder(StringUtf8Coder.of()).apply(ParDo.of(new DoFn<String, String>() {
            @ProcessElement
            public void processElement(ProcessContext context){
                String element = context.element();
                System.out.println("spark Tag " + element);
                context.output(element);
            }
        }));

        resultTuple.get(endWithWorldTag).setCoder(StringUtf8Coder.of()).apply(ParDo.of(new DoFn<String, String>() {
            @ProcessElement
            public void processElement(ProcessContext context){
                String element = context.element();
                System.out.println("world Tag " + element);
                context.output(element);
            }
        }));

        pipeline.run();
    }
}
