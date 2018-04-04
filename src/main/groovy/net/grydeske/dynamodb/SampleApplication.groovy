package net.grydeske.dynamodb

import com.amazonaws.services.dynamodbv2.document.Item

class SampleApplication {

    DynamoDBService dynamoDBService

    SampleApplication(DynamoDBService dynamoDBService) {
        this.dynamoDBService = dynamoDBService
    }

    void runApplication() {
        Scanner sc = new Scanner(System.in)

        String line = sc.nextLine()
        while( line != 'exit' ) {
            switch (line) {
                case 'tables':
                    listTables()
                    break
                case 'load':
                    loadSampleData()
                    break
                case 'id':
                    lookupById(sc)
                    break
                case 'scan':
                    listPokemons()
                    break
                case 'help':
                    listCommands()
                    break
                default:
                    println "Unknown input: ${line}"
                    listCommands()
            }
            line = sc.nextLine()
        }
    }

    void listCommands(){
        println "Commands available:"
        println "tables\nload\nid\nscan\nhelp\nexit"
    }

    void listTables() {
        def tableNames = dynamoDBService.tableNames
        println "Table names (${tableNames.size()})"
        tableNames.each {
           println "Table name: ${it}"
        }
    }

    void lookupById(Scanner sc) {
        println "Which id should we look for?"
        String id = sc.nextLine()
        Item pokemon = dynamoDBService.getById('pokemon', 'Id', id)
        if(pokemon != null){
            show(pokemon)
        }

    }

    void show(Item item){
        Map pokemon = item.asMap()
        println "${pokemon['Name']} - ${pokemon['Type1']}:"
        for(String key: pokemon.keySet()){
            if(key.equals("name") || key.equals("Type1"))
                continue;
            println "- ${key} ${pokemon[key]}"
        }
        println ""
    }
    // Scan
    void listPokemons() {
        List<Item> pokemonItems = dynamoDBService.scanTable('pokemon')
        println "Pokemon List:"
        pokemonItems.each { Item item ->
            Map pokemon = item.asMap()
            println "${pokemon['Name']} - ${pokemon['Type1']}"

        }
    }

    void queryByType(Scanner sc) {
        println "Which type are you looking for?"
        String type = sc.nextLine()

        // TODO Find and print all pokemons of the desired type

    }

    void loadSampleData() {
        def tableName = 'pokemon'
        dynamoDBService.createTable(tableName, 'Id', 'S')
        def first = true
        def headers
        def items = []
        this.getClass().getResource( '/Pokemon.csv' ).text.eachLine {
            def parts = it.split(',')
            if( first) {
                headers = parts
                first = false
            } else {
                if( parts.size() == 13) {
                    Item item = new Item().withPrimaryKey("Id", parts[0])
                            .withString("Name", parts[1] )
                            .withString("Type1", parts[2] )
                            .withString("Type2", parts[3] ?: 'N/A' )
                            .withInt("Total", parts[4] as Integer)
                            .withInt("HP", parts[5] as Integer )
                            .withInt("Attack", parts[6] as Integer )
                            .withInt("Defense", parts[7] as Integer )
                            .withInt("SpAtk", parts[8] as Integer )
                            .withInt("SpDef", parts[9] as Integer )
                            .withInt("Speed", parts[10] as Integer )
                            .withInt("Generation", parts[11] as Integer )
                            .withBoolean("Legendary", parts[12].toBoolean() )

                    items << item

                }
            }
        }
        dynamoDBService.insertDataInTable(tableName, items)
        println "Data loaded :)"
    }
}
