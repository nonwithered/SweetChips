apply plugin: 'maven-publish'
publishing {
    publications {
        maven(MavenPublication) {
            groupId "org.${rootProject.name.toLowerCase()}"
            artifactId project.name
            version 'snapshot'
            from components.java
        }
    }
    repositories {
        maven {
            url = rootProject.ext.buildUri
        }
    }
}
