# Member activation view producer


- Based on kinesis streams
- Events stored in dynamodb

 
## Store events in dynamodb

Required

- A dynamodb table
- From kinesis to dynamodb

### Create a dynamodb table to store member activation events

Table memberActivationsByActivationTime

- Partition key 'activationTime' in following format YYYY-MM-DDTHH
- Sort key activationTimestamp

Created with ARN arn:aws:dynamodb:eu-west-1:307482651216:table/memberActivationsByActivationTime

-- in common sandbox

### From Kinesis to dynamodb

- Need a transformation in kinesis firehose to write in that table 

 

## Create view

- A view is a query on dynamodb 




