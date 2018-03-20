# Member activation view producer

Different approaches to explore to produce the view from events

1. KS + S3 + Athena

## S3 + Athena 

1. Events are stored in a given s3 bucket (https://s3.console.aws.amazon.com/s3/buckets/gg-member-activations-tests)

```
{"activationTimestamp":"2018-03-16T12:50:12.875"}
```

2. You have a Athena Table created

```
CREATE EXTERNAL TABLE IF NOT EXISTS member_activations.member_activations (
  `activationTimestamp` string 
)
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
WITH SERDEPROPERTIES (
  'serialization.format' = '1'
) LOCATION 's3://XXX-bucket/2018/03/'
TBLPROPERTIES ('has_encrypted_data'='false')
``` 

3. Run the query from Athena

```
SELECT * FROM "member_activations"."member_activations" limit 10;
```
- Using athena from [cli](https://sysadmins.co.za/using-the-aws-cli-tools-to-interact-with-amazons-athena-service/)

4. Settings up Tableau to connect Athena

- Know how the integration [works](https://www.tableau.com/about/blog/2017/5/connect-your-s3-data-amazon-athena-connector-tableau-103-71105)
- Install the [driver](https://docs.aws.amazon.com/athena/latest/ug/connect-with-jdbc.html)
- Open Tableau Desktop 10.5 and setup [Athena as Datasource](tableau-setup-athena.png)

*Note on setting up Athena* As for now, Tableau 10.5 does no support credentials coming from assumed roles so you need to 
use non temp credentials. 