package com.yss.beam.hive;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.hcatalog.HCatalogIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hive.hcatalog.data.HCatRecord;

import java.util.HashMap;
import java.util.Map;
/**
 * @author MingZhang Wang
 */
public class BeamHiveModel {
    public static void main(String[] args) {
        Map<String, String> configProperties = new HashMap<>();
        configProperties.put("hive.metastore.uris","thrift://henghe-043:9083");
        configProperties.put("hive.metastore.client.capability.check","false");
        PipelineOptions options = PipelineOptionsFactory.create();
//        options.setRunner(SparkRunner.class);

        Pipeline pipeline = Pipeline.create(options);
        PCollection<HCatRecord> apply = pipeline
                .apply(HCatalogIO.read()
                        .withConfigProperties(configProperties)
                        .withDatabase("default")
                        .withTable("mysql_kakfa"))

                .apply(ParDo.of(new DoFn<HCatRecord, HCatRecord>() {
                    @ProcessElement
                    public void processElement(ProcessContext context) {
                        HCatRecord hCatRecord = context.element();
                        Object o = hCatRecord.get(0);
                        System.out.println(o.toString());
                        context.output(hCatRecord);
                    }
                }));
        System.out.println(apply);
        pipeline.run();
    }

}
