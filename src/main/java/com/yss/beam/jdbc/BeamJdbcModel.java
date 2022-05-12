package com.yss.beam.jdbc;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;

/**
 * 该案例是使用Apache Beam 读写Mysql的案例
 *
 * 该案例读取Mysql数据后，根绝key进行聚合操作，然后累加value，并将value值扩大10倍，最后打印结果后写入Mysql
 *
 */
public class BeamJdbcModel {
    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        //读取Mysql
        PCollection<KV<String, Integer>> resultCollection = pipeline.apply(JdbcIO.<KV<String, Integer>>read()
                        .withDataSourceConfiguration(JdbcIO.DataSourceConfiguration.create(
                                        "com.mysql.jdbc.Driver", "jdbc:mysql://192.168.165.34:3306/development2_3")
                                .withUsername("root")
                                .withPassword("1q2w3eROOT!"))
                        .withQuery("select * from TestBeam")
                //对结果集中的每一条数据进行处理
                        .withRowMapper(new JdbcIO.RowMapper<KV<String, Integer>>() {
                            @Override
                            public KV<String, Integer> mapRow(ResultSet resultSet) throws Exception {
                                String id = resultSet.getString(1);
                                String name = resultSet.getString(2);
                                System.out.println(id + ":" + name);
                                return KV.of(name, 1);
                            }
                        }))
                // 根据key聚合
                .apply(GroupByKey.<String, Integer>create())
                // 对聚合后的结果进行处理
                .apply(MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.integers())).via(e -> {
                    Iterable<Integer> value = e.getValue();
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    Iterator<Integer> iterator = e.getValue().iterator();
                    Integer i = 0;
                    while (iterator.hasNext()) {
                        i += iterator.next();
                    }
                    return KV.of(e.getKey(), i*10);
                }))
                // 自定义算子打印结果集
                .apply(ParDo.of(new DoFn<KV<String, Integer>, KV<String, Integer>>() {
                    @ProcessElement
                    public void processElement(ProcessContext context) {
                        // 从管道中取出的每个元素
                        KV<String, Integer> element = context.element();
                        System.out.println(element);
                        if (element != null) {
                            context.output(element);
                        }
                    }
                }));

        // 将结果集写入数据库
        resultCollection.apply(JdbcIO.<KV<String,Integer>>write()
                .withDataSourceConfiguration(JdbcIO.DataSourceConfiguration.create(
                                "com.mysql.jdbc.Driver", "jdbc:mysql://192.168.165.34:3306/development2_3")
                        .withUsername("root")
                        .withPassword("1q2w3eROOT!"))
                .withStatement("insert into TestBeamCount values(?,?)")
                .withPreparedStatementSetter(new JdbcIO.PreparedStatementSetter<KV<String, Integer>>() {
                    @Override
                    public void setParameters(KV<String, Integer> element, PreparedStatement preparedStatement) throws Exception {
                        preparedStatement.setString(1,element.getKey());
                        preparedStatement.setInt(2,element.getValue());
                    }
                }));

        pipeline.run();
    }
}
