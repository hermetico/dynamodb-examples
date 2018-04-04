= DynamoDB Example

Example of running DynamoDB locally, for use as demo and exercises in the DM566 course at IMADA, SDU.

== Pokemon Example Data

The dataset is from here: https://www.kaggle.com/shoduro/pick-your-pokemon/data

== Exercises

The code is in Groovy, but you can program JAVA if you prefer this. Very few JAVA constructs are invalid Groovy (Up to JAVA 7).

You should find help in the documentation: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GettingStarted.Java.html[]

. Clone the repo, and try to run the code using `./gradlew run`
** Possible inputs are:
*** *tables* Lists the tables in the database
*** *load* Loads the Pokemon dataset
*** *scan* Lists the pokemon with their name and type (Type1)
*** *id* Asks which id of pokemon you will see. #42 is _Golbat_
.[X] Make the output format of the id task display prettier, and not like json.
** HINT: See how scan is made
. Try to implement querying, so you can query by a type, and only gets pokemons of that type displayed.
. Add a seen column to the pokemon, and add a method where you can increment the number of times you have seen the different pokemons.
. Implement querying where you get listed all pokemons you have seen less times than/more times that a supplied number.
. Add a delete method where you can remove a pokemon from the dataset
. Add information on where you have seen a pokemon, so you can get a list of all the places you have seen it
** Make also an aggegate list, where you group by the location.
. Extend the location with a username, so you can use the database for many users
** And extract who have seen the pokemon how many times.

TIP: There should be plenty of exercises - this is by design, so you don't run out of ideas to implement in the tutorial session.