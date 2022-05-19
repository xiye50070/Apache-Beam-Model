package com.yss.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.ListCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.join.*;
import org.apache.beam.sdk.values.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BeamMutiInput {
    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        final TupleTag<Long> ATag = new TupleTag<Long>(){};
        final TupleTag<Long> BTag = new TupleTag<Long>(){};

        List<Coder<?>> stringUtf8Coders = new ArrayList<>();

        PCollection<KV<String, Long>> inputA = pipeline.apply(Create.of("Ahello Aworld", "Ahello Aflink", "Ahello Aflink spark"))
                .apply(FlatMapElements.into(TypeDescriptors.strings()).via(line -> Arrays.asList(line.split(" "))))
                .apply(Count.perElement());
        PCollection<KV<String, Long>> inputB = pipeline.apply(Create.of("Bhello Aworld", "Bhello Aflink", "Bhello Aflink spark"))
                .apply(FlatMapElements.into(TypeDescriptors.strings()).via(line -> Arrays.asList(line.split(" "))))
                .apply(Count.perElement());

        PCollection<KV<String, CoGbkResult>> joinedCollection = KeyedPCollectionTuple.of(ATag, inputA)
                .and(BTag, inputB)
                .apply(CoGroupByKey.<String>create())
                .apply(ParDo.of(new DoFn<KV<String, CoGbkResult>, KV<String, CoGbkResult>>() {
            @ProcessElement
            public void processElement(ProcessContext context){
                KV<String, CoGbkResult> element = context.element();
                System.out.println(element);
                context.output(element);
            }
        }));

        pipeline.run();
    }
}
