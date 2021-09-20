# Guardium Snowflake Connector
A Guardium Universal Connector filter plugin for Snowflake. The connector allows people to
monitor SQL occuring in their Snowflake environments by providing a feed of events to
Guardium using the new [V11.3 Universal Connector functionality](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/g_universal_connector.html).

## Building the Plugin
A good resource for compiling and packaging this plugin is the documentation outlining 
[IBM's MongoDB filter](https://github.com/IBM/logstash-filter-mongodb-guardium), which 
this project is based on.

## Guardium Version
You should use this plugin with Guardium V11.4. While it technically worked in V11.3 there were
issues around both parsing and configuring the plugin that have been greatly improved in V11.4.

## Installing the Plugin
This section of the readme outlines the installation process.

### 1. Install the Plugin
The first step is to install a plug-in pack that includes this plugin. Instructions on how
to do so can be found [here](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/test_filter_guardium.html).

### 2. Upload JDBC Driver and Initialize :sql_last_value
Once installed, upload a Snowflake JDBC driver as described [here](https://www.ibm.com/support/knowledgecenter/SSMPHH_11.3.0/com.ibm.guardium.doc.stap/guc/test_filter_guardium.html).

We have tested with Snowflake JDBC driver V3.9.2. You can file the JDCB driver [here](https://repo1.maven.org/maven2/net/snowflake/snowflake-jdbc/3.9.2/).

Next, to prevent Snowflake errors from occurring when querying data that is older than a 
week, create a file called "metadata" and initialize it to a time that is at a date sooner than a week ago.
Use the following format as an example:
```
--- 2020-12-22 11:23:20.085000000 -00:00
```
That should be the only contents of the "metadata" file. Upload it the same way you uploaded
the JDBC driver. It is used later to keep track of the :sql_last_value parameter

### 3. Configure the Input and Filter Plugins
Then, configure a JDBC Logstash **input** source using this as a template. Replace all 
values located in angle brackets.
```ruby
jdbc {
    jdbc_connection_string => "jdbc:snowflake://<id>.<region>.<provider>.snowflakecomputing.com/?warehouse=<warehouse>&db=<database>"
    jdbc_user => "<username>"
    jdbc_password => "<password"
    jdbc_validate_connection => true
    jdbc_driver_class => "Java::net.snowflake.client.jdbc.SnowflakeDriver"
    jdbc_driver_library => "/usr/share/logstash/logstash-core/lib/jars/snowflake-jdbc-3.9.2.jar"
    use_column_value  => true
    tracking_column_type => "timestamp"
    last_run_metadata_path => "/usr/share/logstash/third_party/metadata"
    record_last_run => true
    schedule =>  "* * * * *" 
    tracking_column => "end_time"
    plugin_timezone => "local"
    add_field => {"server_host_name" => "<id>.<region>.<provider>.snowflakecomputing.com"}
    statement => "
    	select * from table(
    					  information_schema.query_history(
    					  RESULT_LIMIT => 10000,
    					  END_TIME_RANGE_START => to_timestamp_ltz(:sql_last_value || ' -0000')
    			   )
    		)
    		where execution_status <> 'RUNNING'
    	AND end_time > :sql_last_value  || ' -0000'
    	ORDER BY END_TIME
    "
}


```
The user you define in the jdbc_user parameter must have enough permissions to execute the SQL in statement area.
You are encouraged to test this first by replacing :sql_last_value with a string literal and running
this against Snowflake with the user in question.


The configuration for the Snowflake filter plugin is simpler:
```ruby
guardium_snowflake_filter{
}
```

## Known Issues and Limitations
This is a list of known issues and limitations. Not all issue are resolvable as the data the connector
can provide is limitted by what Snowflake keeps track of in its audit logs.

1. Client IPs are not reported because they are not part of the Snowflake audit stream
2. Server IPs are also not reported because they are not part of the audit stream. That said, the "add_field" clause in the example shown above adds a user defined Server Host Name that can be used in reports and policies if desired
3. Source Programs are not reported because they are not part of the Snowflake audit stream
4. "OS User" is also not provided by the Snowflake audit stream, but instead we made the decision to populate "OS User" with the current user's role in Snowflake as that might be useful information


Author: John Haldeman



