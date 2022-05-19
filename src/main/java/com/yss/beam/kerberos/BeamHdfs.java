package com.yss.beam.kerberos;

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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.Arrays;

import java.io.IOException;
import java.util.Arrays;
/**
 * @author MingZhang Wang
 */
public class BeamHdfs {
    public static void main(String[] args) {
        authKerberos();
        String[] args1 = new String[] {
                "--hdfsConfiguration=[{\"fs.defaultFS\":\"hdfs://192.168.165.47:8020\",\"dfs.namenode.kerberos.principal\":\"henghe/_HOST@HENGHE.COM\"}]" };
        HadoopFileSystemOptions options =
                PipelineOptionsFactory.fromArgs(args1).withValidation().as(
                        HadoopFileSystemOptions.class);

        Pipeline pipeline = Pipeline.create(options);
        pipeline.apply(TextIO.read().from("hdfs://192.168.165.47:8020/tmp/TestTxt.txt"))
                .apply(FlatMapElements.into(TypeDescriptors.strings()).via(line -> Arrays.asList(line.split(" "))))
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
//                .apply("write",TextIO.write().to("hdfs://henghe-042:9002/user/TestTxt2.txt"));
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
