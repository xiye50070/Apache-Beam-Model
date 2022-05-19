package com.yss.beam.kerberos;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.hcatalog.HCatalogIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hive.hcatalog.data.HCatRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 * @author MingZhang Wang
 */
public class BeamHive {
    public static void main(String[] args) {
        authKerberos();
        Map<String, String> configProperties = new HashMap<>();
        configProperties.put("hive.metastore.uris","thrift://192.168.165.60:9083");
        configProperties.put("hive.metastore.client.capability.check","false");
        PipelineOptions options = PipelineOptionsFactory.create();
//        options.setRunner(SparkRunner.class);

        Pipeline pipeline = Pipeline.create(options);
        PCollection<HCatRecord> apply = pipeline
                .apply(HCatalogIO.read()
                        .withConfigProperties(configProperties)
                        .withDatabase("default")
                        .withTable("developtest"))

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

    public static Configuration authKerberos(){
        Configuration configuration = new Configuration();
        configuration.set("hadoop.security.authentication", "kerberos");
        configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        configuration.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
        configuration.set("dfs.namenode.kerberos.principal", "henghe@HENGHE.COM");
        UserGroupInformation.setConfiguration(configuration);
        try {
            UserGroupInformation.loginUserFromKeytab("henghe@HENGHE.COM", "/Users/wangmingzhang/Documents/Work/IDEAProject/Apache-Beam-Model/src/main/resources/henghe.tenant.keytab");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configuration;
    }
}
