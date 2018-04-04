package net.grydeske.dynamodb

class Example {

    static void main(String[] args) {

        DynamoDBService dynamoDBService = new DynamoDBService()

        dynamoDBService.startupDatabaseServer()

        try {
            new SampleApplication(dynamoDBService).runApplication()
        } finally {
            dynamoDBService.shutdownDatabase()
        }
    }
}
