# Sariska Meet Java

Sariska Media Transport provides powerful Java API's for developing real-time applications.

This is a sample app to  showcase basic functionality of Sariska Media API's which can be leveraged for your Native Android development in Java and Kotlin.

## Instructions

The sample app lets you create a room with a name of your choice. Once the user clicks on the start meeting button, a conference is created and any other user with the same room name can join in.

1. Generate Github token for accessing github packages
> In order to access github packages, you need to authenticate using github token, the token should have at the least Packages Read access.

2. Replace the github token into the local.properties file
> In order to make sure that you do not accidentally push your github secrets on your remote repository, add the github username and github token into local.properties as shown below

```
...
githubUsername={your-github-username}
githubPassword={your-github-token}
...

```

3. Build Gradle
> Sync gradle once you are done with the above steps. All dependencies should resolve themselves now.

4. Start the application
> You can now start the application on your device.

## Sample Video
https://user-images.githubusercontent.com/22401307/194180222-04a18d6b-85a1-4cdf-831d-6feede2eb09b.mp4
