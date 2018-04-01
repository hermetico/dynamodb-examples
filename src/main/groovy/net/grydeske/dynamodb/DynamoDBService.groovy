package net.grydeske.dynamodb

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.ScanOutcome
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.TableCollection
import com.amazonaws.services.dynamodbv2.document.internal.ListTablesPage
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.CreateTableResult
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.ListTablesResult
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput

class DynamoDBService {

    DynamoDB dynamodb = null
    DynamoDBProxyServer server = null;

    void startupDatabaseServer() {
        final String[] localArgs = ["-inMemory"].toArray()

        server = ServerRunner.createServerFromCommandLineArgs(localArgs);
        server.start()

        def  auth = new BasicAWSCredentials("fakeKey", "fakeSecret")
        def client = new AmazonDynamoDBClient(auth)
        client.signerRegionOverride = "us-east-1"
        client.setEndpoint("http://localhost:8000")

        dynamodb = new DynamoDB(client)
    }

    void shutdownDatabase() {
        println "Shutting down database"
        if(server != null) {
            server.stop()
        }

    }

    void createTable(String tableName, String partitionKeyName, String partitionKeyType) {

        def keySchema = [new KeySchemaElement().withAttributeName(partitionKeyName).withKeyType(KeyType.HASH)]

        def attributeDefinitions = [new AttributeDefinition().withAttributeName(partitionKeyName).withAttributeType(partitionKeyType)]

        CreateTableRequest request = new CreateTableRequest().withTableName(tableName).withKeySchema(keySchema)
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(10)
                    .withWriteCapacityUnits(5));

        request.setAttributeDefinitions(attributeDefinitions);

        println("Issuing CreateTable request for " + tableName);
        Table table = dynamodb.createTable(request);
        println("Waiting for $tableName to be created...this may take a while...")
        table.waitForActive()
        println "Table created"
    }

    List<String> getTableNames() {
        List<String> tableNames = []

        TableCollection<ListTablesResult> res =  dynamodb.listTables()

        res.pages().each { ListTablesPage page ->
            tableNames.addAll( page.getLowLevelResult().tableNames )
        }
        tableNames
    }

    void insertDataInTable(String tableName, List<Item> items) {
        Table table = dynamodb.getTable(tableName)

        items.each {
            table.putItem(it)
        }
    }

    void getById(String tableName, String keyName, String keyValue) {
        Table table = dynamodb.getTable(tableName);

        GetItemSpec spec = new GetItemSpec().withPrimaryKey(keyName, keyValue)

        try {
            println("Attempting to read the item...");
            Item outcome = table.getItem(spec)
            println("GetItem succeeded: " + outcome)

        } catch (Exception e) {
            println("Unable to read item: ${keyName}=${keyValue}")
            println(e.getMessage())
        }

    }

    List<Item> scanTable(String tableName) {
        Table table = dynamodb.getTable(tableName);

        ScanSpec scanSpec = new ScanSpec()

        try {
            ItemCollection<ScanOutcome> items = table.scan(scanSpec)

            return items.collect { Item item -> item }
        }
        catch (Exception e) {
            println("Unable to scan the table:")
            println(e.getMessage())
        }
        []
    }

    void query( String tableName) {
        Table table = dynamodb.getTable(tableName);

        HashMap<String, Object> valueMap = [":yyyy", 1985]

        QuerySpec querySpec = new QuerySpec()
                .withConditionalOperator()
                .withKeyConditionExpression("#yr = :yyyy")
                .withNameMap(nameMap)
                .withValueMap(valueMap);

        ItemCollection<QueryOutcome> items = null;
        Iterator<Item> iterator = null;
        Item item = null;

        try {
            System.out.println("Movies from 1985");
            items = table.query(querySpec);

            iterator = items.iterator();
            while (iterator.hasNext()) {
                item = iterator.next();
                System.out.println(item.getNumber("year") + ": " + item.getString("title"));
            }

        }
        catch (Exception e) {
            System.err.println("Unable to query movies from 1985");
            System.err.println(e.getMessage());
        }

    }

    void getItems(String tablename) {
//        dynamodb.getTable('pokemon').query()
    }

}
