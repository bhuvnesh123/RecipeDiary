# RecipeDiary
![screen1](https://user-images.githubusercontent.com/26456376/129476174-4555457b-1782-4545-809f-b3f728572ee0.png)

The sample project named RecipeDairy allows user to write recipes and save/read them to/from app's local database as your personalized recipe collection.Database is synchronized with Network(Firebase Firestore).
This project is made of using all the latest technologies of Modern Android Development.

​Language- Kotlin

​Architecture - Model-View-Intent(MVI)

​Database- Room

​Async- Kotlin Flows and channels, Coroutines

​Dependency Injection-Hilt

​Network - Firebase Firestore

​Testing-Unit tests

​Etc- Jetpack Navigation, Gradle Dependency Management

# Running this app:
1.To run this app you will need to create a firebase project and link it up with the project.
https://firebase.google.com/docs/android/setup

2.Create Firebase Authentication user and replace the UserId generated in RecipeFirestoreServiceImpl file.

 const val USER_ID =
            "XXXXXXXXXXXXXXXXXXXXXXXXXXX" // hardcoded for single user..replace with your firebase USER UID
 
 
 # Credits:
 1.https://codingwithmitch.com/
 
 2.https://gist.github.com/bmc08gt/fca95db3bf9fcf255d76f03ec10ea3f9
            
