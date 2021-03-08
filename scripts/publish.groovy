apply plugin: 'maven-publish'
publishing {
    publications {
        maven(MavenPublication) {
            groupId "org.${rootProject.name.toLowerCase()}"
            artifactId project.name
            version 'develop'
            from components.java
        }
    }
    repositories {
        maven {
            url = rootProject.ext.buildUri
        }
    }
}

sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8