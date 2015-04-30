package com.widowcrawler.index.module;

import com.google.inject.AbstractModule;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory;
import com.netflix.governator.annotations.Modules;
import com.widowcrawler.core.module.DynamoDBModule;
import com.widowcrawler.core.module.WidowCoreModule;
import com.widowcrawler.core.worker.WorkerProvider;
import com.widowcrawler.index.IndexWorkerProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Scott Mansfield
 */
@Modules(include={WidowCoreModule.class, DynamoDBModule.class})
public class WidowIndexModule extends AbstractModule {
    @Override
    public void configure() {
        // TODO: Make this all config
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://widow-test.c0endjsljhny.us-west-2.rds.amazonaws.com");
        dataSource.setPort(3306);
        dataSource.setDatabaseName("widow");
        dataSource.setUser("masterwidow");
        dataSource.setPassword("thisismypassword");

        bind(DataSource.class).toInstance(dataSource);

        bind(WorkerProvider.class).to(IndexWorkerProvider.class).asEagerSingleton();
    }
}
