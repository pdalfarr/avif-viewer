    # Gradle Sync
    ./gradlew help --refresh-dependencies  
    
    # Test download dependencies
    ./gradlew dependencies
    
    ./gradlew build
    
    # Test plugin
    ./gradlew runIde
    ./gradlew runIde --no-build-cache
    ./gradlew runIde --no-build-cache --rerun-tasks
    
    ./gradlew buildPlugin

    ./gradlew test

     #Display log info
    ./gradlew test --info

    ./gradlew test --tests "be.dalfarra.avifviewer.editor.MyPluginTest"
    ./gradlew test --tests "*IntegrationTest"
    ./gradlew test --info

